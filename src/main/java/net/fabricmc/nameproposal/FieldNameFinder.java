/*
 * Copyright (c) 2016, 2021 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.fabricmc.nameproposal;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.SourceInterpreter;
import org.objectweb.asm.tree.analysis.SourceValue;

import net.fabricmc.nameproposal.field.FieldData;
import net.fabricmc.nameproposal.field.nameprovider.FieldNameProvider;

public class FieldNameFinder {
	public Map<MappingEntry, String> findNames(Iterable<byte[]> classes, FieldNameProvider nameProvider) throws Exception {
		Map<String, List<MethodNode>> methods = new HashMap<>();
		Map<String, Set<String>> enumFields = new HashMap<>();

		for (byte[] data : classes) {
			ClassReader reader = new ClassReader(data);
			NameFinderVisitor vClass = new NameFinderVisitor(Constants.ASM_VERSION, enumFields, methods);
			reader.accept(vClass, ClassReader.SKIP_FRAMES);
		}

		return findNames(enumFields, methods, nameProvider);
	}

	public Map<MappingEntry, String> findNames(Map<String, Set<String>> allEnumFields, Map<String, List<MethodNode>> classes, FieldNameProvider nameProvider) {
		Objects.requireNonNull(allEnumFields);
		Objects.requireNonNull(classes);
		Objects.requireNonNull(nameProvider);

		Analyzer<SourceValue> analyzer = new Analyzer<>(new SourceInterpreter());
		Map<MappingEntry, String> fieldNames = new HashMap<>();

		for (Map.Entry<String, List<MethodNode>> entry : classes.entrySet()) {
			String owner = entry.getKey();
			Set<String> enumFields = allEnumFields.getOrDefault(owner, Collections.emptySet());

			Set<String> fieldNamesUsed = new HashSet<>();
			Set<String> fieldNamesDuplicate = new HashSet<>();

			for (MethodNode mn : entry.getValue()) {
				findMethodNames(nameProvider, analyzer, fieldNames, owner, enumFields, fieldNamesUsed, fieldNamesDuplicate, mn);
			}
		}

		return fieldNames;
	}

	private void findMethodNames(FieldNameProvider nameProvider, Analyzer<SourceValue> analyzer, Map<MappingEntry, String> fieldNames, String owner, Set<String> enumFields, Set<String> fieldNamesUsed, Set<String> fieldNamesDuplicate, MethodNode mn) {
		Frame<SourceValue>[] frames;

		try {
			frames = analyzer.analyze(owner, mn);
		} catch (AnalyzerException e) {
			throw new RuntimeException(e);
		}

		InsnList instrs = mn.instructions;

		for (int i = 1; i < instrs.size(); i++) {
			AbstractInsnNode instr1 = instrs.get(i - 1);
			AbstractInsnNode instr2 = instrs.get(i);

			if (instr2.getOpcode() != Opcodes.PUTSTATIC) continue;
			FieldInsnNode fieldNode = (FieldInsnNode) instr2;

			if (instr1.getOpcode() != Opcodes.INVOKESTATIC && instr1.getOpcode() != Opcodes.INVOKESPECIAL) continue;
			if (!(instr1 instanceof MethodInsnNode methodNode)) continue;

			var frame = frames[i - 1];
			var args = new Object[frame.getStackSize()];

			for (int j = 0; j < frame.getStackSize(); j++) {
				SourceValue sv = frame.getStack(j);

				for (AbstractInsnNode ci : sv.insns) {
					if (ci instanceof LdcInsnNode node && node.cst instanceof String arg) {
						args[j] = arg;
					}
				}
			}

			boolean isEnum = enumFields.contains(fieldNode.desc + fieldNode.name);
			var field = new FieldData(fieldNode.owner, fieldNode.name, fieldNode.desc, methodNode.owner, methodNode.name, args, true, isEnum);
			var name = nameProvider.getName(field);

			if (name != null) {
				if (!fieldNamesDuplicate.contains(name)) {
					if (!fieldNamesUsed.add(name)) {
						System.out.println("Warning: Duplicate field name '" + name + "' was proposed! (" + field + ")");
						fieldNamesDuplicate.add(name);
						fieldNamesUsed.remove(name);
					}
				}

				if (fieldNamesUsed.contains(name)) {
					fieldNames.put(new MappingEntry(fieldNode.owner, fieldNode.name, fieldNode.desc), name);
				}
			}
		}
	}
}
