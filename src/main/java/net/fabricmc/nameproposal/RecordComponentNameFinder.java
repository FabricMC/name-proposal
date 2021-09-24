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

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;

public final class RecordComponentNameFinder extends ClassVisitor {
	// comp_x -> name
	private final Map<String, String> recordNames;
	private final List<Component> recordComponents = new LinkedList<>();

	public RecordComponentNameFinder(int api, Map<String, String> recordNames) {
		super(api);
		this.recordNames = recordNames;
	}

	@Override
	public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
		if (name.startsWith("comp_")) {
			recordComponents.add(new Component(name, descriptor));
		}

		return super.visitField(access, name, descriptor, signature, value);
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
		if ("toString".equals(name) && "()Ljava/lang/String;".equals(descriptor)) {
			return new ToStringVisitor(api);
		}

		return super.visitMethod(access, name, descriptor, signature, exceptions);
	}

	private class ToStringVisitor extends MethodVisitor {
		ToStringVisitor(int api) {
			super(api);
		}

		@Override
		public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
			if (bootstrapMethodArguments.length >= 2) {
				Object bsm1 = bootstrapMethodArguments[1];

				if (bsm1 instanceof String) {
					String[] names = ((String) bsm1).split(";");

					if (names.length == recordComponents.size()) {
						for (int i = 0; i < recordComponents.size(); i++) {
							String intermediaryName = recordComponents.get(i).name();
							String previous = recordNames.put(intermediaryName, names[i]);

							if (previous != null) {
								throw new RuntimeException("Duplicate record component names for: " + intermediaryName);
							}
						}
					}
				}
			}

			super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
		}
	}

	private record Component(String name, String desc) {
	}
}
