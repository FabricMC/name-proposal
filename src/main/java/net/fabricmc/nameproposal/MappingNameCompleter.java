/*
 * Copyright (c) 2021 FabricMC
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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import net.fabricmc.mappingio.MappedElementKind;
import net.fabricmc.mappingio.MappingReader;
import net.fabricmc.mappingio.MappingWriter;
import net.fabricmc.mappingio.format.MappingFormat;
import net.fabricmc.mappingio.tree.MappingTree;
import net.fabricmc.mappingio.tree.MemoryMappingTree;

public class MappingNameCompleter {
	// <intermediaryJar> <inputYarnMappings> <inputIntermediaryMappings> <outputYarnMappings>
	public static void main(String[] args) throws IOException {
		completeNames(Paths.get(args[0]), Paths.get(args[1]), Paths.get(args[2]), Paths.get(args[3]));
	}

	public static void completeNames(Path intermediaryJar, Path inputYarnMappings, Path inputIntermediaryMappings, Path outputYarnMappings) throws IOException {
		NameFinder nameFinder = new NameFinder();

		acceptJar(nameFinder, intermediaryJar);

		// We need the full intermediary mappings on their own to lookup the record component's root fields/methods.
		nameFinder.acceptIntermediaryMappings(readMappings(inputIntermediaryMappings));

		Map<MappingEntry, String> fieldNames = nameFinder.getFieldNames();
		Map<MappingEntry, String> methodNames = nameFinder.getMethodNames();
		Map<String, String> recordNames = nameFinder.getRecordNames();

		System.out.printf("Found %d field names%n", fieldNames.size());
		System.out.printf("Found %d method names%n", methodNames.size());
		System.out.printf("Found %d record names%n", recordNames.size());

		final MemoryMappingTree yarn = readMappings(inputYarnMappings);
		final int yarnIntermediaryNs = yarn.getNamespaceId("intermediary");
		final int yarnNamedNs = yarn.getNamespaceId("named");

		for (Map.Entry<MappingEntry, String> entry : fieldNames.entrySet()) {
			MappingEntry mappingEntry = entry.getKey();

			// Ensure there is a class mapping for this
			yarn.visitClass(mappingEntry.owner());

			MemoryMappingTree.ClassMapping classMapping = yarn.getClass(mappingEntry.owner(), yarnIntermediaryNs);

			yarn.visitField(mappingEntry.name(), mappingEntry.desc());
			MappingTree.FieldMapping fieldMapping = Objects.requireNonNull(classMapping.getField(mappingEntry.name(), mappingEntry.desc(), yarnIntermediaryNs), "Could not find field");
			String yarnFieldName = fieldMapping.getName(yarnNamedNs);

			if (yarnFieldName == null || yarnFieldName.startsWith("field_") || yarnFieldName.startsWith("comp_")) {
				// Set a new dst name if it doesn't have one, or matches intermediary
				yarn.visitDstName(MappedElementKind.FIELD, yarnNamedNs, entry.getValue());
			}
		}

		Set<String> writtenMethods = new HashSet<>();

		for (Map.Entry<MappingEntry, String> entry : methodNames.entrySet()) {
			if (!writtenMethods.add(entry.getKey().name())) {
				throw new RuntimeException("Duplicate method name: " + entry.getKey().name());
			}

			MappingEntry mappingEntry = entry.getKey();

			// Ensure there is a class mapping for this
			yarn.visitClass(mappingEntry.owner());

			MemoryMappingTree.ClassMapping classMapping = yarn.getClass(mappingEntry.owner(), yarnIntermediaryNs);

			yarn.visitMethod(mappingEntry.name(), mappingEntry.desc());
			MappingTree.MethodMapping methodMapping = Objects.requireNonNull(classMapping.getMethod(mappingEntry.name(), mappingEntry.desc(), yarnIntermediaryNs), "Could not find method");
			String yarnFieldName = methodMapping.getName(yarnNamedNs);

			if (yarnFieldName == null || yarnFieldName.startsWith("method_") || yarnFieldName.startsWith("comp_")) {
				// Set a new dst name if it doesn't have one, or matches intermediary
				yarn.visitDstName(MappedElementKind.METHOD, yarnNamedNs, entry.getValue());
			}
		}

		try (MappingWriter mappingWriter = MappingWriter.create(outputYarnMappings, MappingFormat.TINY_2)) {
			yarn.accept(mappingWriter);
		}
	}

	private static void acceptJar(NameFinder nameFinder, Path jar) throws IOException {
		try (JarInputStream jarInputStream = new JarInputStream(Files.newInputStream(jar))) {
			JarEntry entry;

			while ((entry = jarInputStream.getNextJarEntry()) != null) {
				if (!entry.getName().endsWith(".class")) {
					continue;
				}

				ClassReader reader = new ClassReader(jarInputStream);
				ClassNode classNode = new ClassNode();
				reader.accept(classNode, 0);

				nameFinder.accept(classNode);
			}
		}
	}

	private static MemoryMappingTree readMappings(Path path) throws IOException {
		MemoryMappingTree mappingTree = new MemoryMappingTree();
		MappingReader.read(path, mappingTree);
		return mappingTree;
	}
}
