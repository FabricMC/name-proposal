/*
 * Copyright (c) 2023 FabricMC
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

package net.fabricmc.nameproposal.field.predicate;

import java.util.function.Function;

import com.mojang.serialization.Codec;

import net.fabricmc.nameproposal.registry.Registry;

public final class FieldPredicates {
	private static final Registry<Codec<? extends FieldPredicate>> REGISTRY = new Registry<>();
	public static final Codec<FieldPredicate> CODEC = REGISTRY.dispatchStable(FieldPredicate::getCodec, Function.identity());

	public static final Codec<ArgumentFieldPredicate> ARGUMENT = register("nameproposal:argument", ArgumentFieldPredicate.CODEC);
	public static final Codec<DescriptorFieldPredicate> DESCRIPTOR = register("nameproposal:descriptor", DescriptorFieldPredicate.CODEC);
	public static final Codec<EnumFieldPredicate> ENUM = register("nameproposal:enum", EnumFieldPredicate.CODEC);
	public static final Codec<InternalInitFieldPredicate> INTERNAL_INIT = register("nameproposal:internal_init", InternalInitFieldPredicate.CODEC);
	public static final Codec<MethodNameFieldPredicate> METHOD_NAME = register("nameproposal:method_name", MethodNameFieldPredicate.CODEC);
	public static final Codec<MethodOwnerFieldPredicate> METHOD_OWNER = register("nameproposal:method_owner", MethodOwnerFieldPredicate.CODEC);
	public static final Codec<NameFieldPredicate> NAME = register("nameproposal:name", NameFieldPredicate.CODEC);
	public static final Codec<OwnerFieldPredicate> OWNER = register("nameproposal:owner", OwnerFieldPredicate.CODEC);
	public static final Codec<StaticFieldPredicate> STATIC = register("nameproposal:static", StaticFieldPredicate.CODEC);

	private FieldPredicates() {
		return;
	}

	public static <T extends FieldPredicate> Codec<T> register(String id, Codec<T> codec) {
		REGISTRY.register(id, codec);
		return codec;
	}

	public static void init() {
		return;
	}
}
