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

package net.fabricmc.nameproposal.registry;

import java.util.List;
import java.util.function.Function;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

public final class Codecs {
	private Codecs() {
		return;
	}

	public static <T> Codec<List<T>> listOrUnit(Codec<T> codec) {
		return Codec.either(codec.listOf(), codec).xmap(either -> {
			return either.map(Function.identity(), List::of);
		}, list -> {
			return list.size() == 1 ? Either.right(list.get(0)) : Either.left(list);
		});
	}

	public static <T> Codec<T> validate(Codec<T> codec, Function<T, String> validator) {
		Function<T, DataResult<T>> toFrom = value -> {
			String error = validator.apply(value);
			return error == null ? DataResult.success(value) : DataResult.error(() -> error);
		};

		return codec.flatXmap(toFrom, toFrom);
	}
}
