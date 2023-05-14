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

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

import net.fabricmc.nameproposal.field.nameprovider.FieldNameProviders;
import net.fabricmc.nameproposal.field.predicate.FieldPredicates;

public class Registry<T> implements Codec<T> {
	private final BiMap<String, T> map = HashBiMap.create();

	public T get(String id) {
		return this.map.get(id);
	}

	public String getId(T value) {
		return this.map.inverse().get(value);
	}

	public void register(String id, T value) {
		this.map.put(id, value);
	}

	@Override
	public <U> DataResult<Pair<T, U>> decode(DynamicOps<U> ops, U input) {
		return Codec.STRING.decode(ops, input).flatMap(pair -> {
			String id = pair.getFirst();
			T value = this.get(id);

			if (value == null) {
				return DataResult.error(() -> "Registry does not contain ID '" + id + "'");
			}

			return DataResult.success(pair.mapFirst(this::get));
		});
	}

	@Override
	public <U> DataResult<U> encode(T value, DynamicOps<U> ops, U prefix) {
		var id = this.getId(value);

		if (id == null) {
			return DataResult.error(() -> "Registry does not contain value '" + value + "'");
		}

		return ops.mergeToPrimitive(prefix, ops.createString(id));
	}

	public static void init() {
		FieldNameProviders.init();
		FieldPredicates.init();
	}
}
