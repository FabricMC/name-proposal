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

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.fabricmc.nameproposal.field.FieldData;
import net.fabricmc.nameproposal.registry.Codecs;

public record SequenceFieldNameProvider(List<FieldNameProvider> nameProviders) implements FieldNameProvider {
	protected static final Codec<SequenceFieldNameProvider> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
			Codecs.listOrUnit(FieldNameProviders.CODEC).fieldOf("name_providers").forGetter(SequenceFieldNameProvider::nameProviders)
		).apply(instance, SequenceFieldNameProvider::new);
	});

	@Override
	public String getName(FieldData field) {
		for (FieldNameProvider nameProvider : nameProviders) {
			String name = nameProvider.getName(field);
			if (name != null) return name;
		}

		return null;
	}

	@Override
	public Codec<SequenceFieldNameProvider> getCodec() {
		return CODEC;
	}

	@Override
	public String toString() {
		return this.nameProviders.toString();
	}
}
