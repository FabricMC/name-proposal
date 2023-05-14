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

package net.fabricmc.nameproposal.field.nameprovider;

import java.util.function.Function;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;

import net.fabricmc.nameproposal.registry.Registry;

public final class FieldNameProviders {
	private static final Registry<Codec<? extends FieldNameProvider>> REGISTRY = new Registry<>();

	public static final Codec<ConstantFieldNameProvider> STRING_CODEC = Codec.STRING.xmap(ConstantFieldNameProvider::new, nameProvider -> nameProvider.name);
	public static final Codec<FieldNameProvider> DISPATCH_CODEC = REGISTRY.dispatchStable(FieldNameProvider::getCodec, Function.identity());
	public static final Codec<SequenceFieldNameProvider> LIST_CODEC = Codec.list(DISPATCH_CODEC).xmap(SequenceFieldNameProvider::new, nameProvider -> nameProvider.nameProviders);

	public static final Codec<FieldNameProvider> CODEC = Codec.either(Codec.either(STRING_CODEC, DISPATCH_CODEC).xmap(either -> {
		return either.map(Function.identity(), Function.identity());
	}, nameProvider -> {
		return nameProvider instanceof ConstantFieldNameProvider constant ? Either.left(constant) : Either.right(nameProvider);
	}), LIST_CODEC).xmap(either -> {
		return either.map(Function.identity(), Function.identity());
	}, Either::left);

	public static final Codec<ConditionalFieldNameProvider> CONDITIONAL = register("nameproposal:conditional", ConditionalFieldNameProvider.CODEC);
	public static final Codec<ConstantFieldNameProvider> CONSTANT = register("nameproposal:constant", ConstantFieldNameProvider.CODEC);
	public static final Codec<SequenceFieldNameProvider> SEQUENCE = register("nameproposal:sequence", SequenceFieldNameProvider.CODEC);
	public static final Codec<StringArgumentFieldNameProvider> STRING_ARGUMENT = register("nameproposal:string_argument", StringArgumentFieldNameProvider.CODEC);

	private FieldNameProviders() {
		return;
	}

	public static <T extends FieldNameProvider> Codec<T> register(String id, Codec<T> codec) {
		REGISTRY.register(id, codec);
		return codec;
	}

	public static void init() {
		return;
	}
}
