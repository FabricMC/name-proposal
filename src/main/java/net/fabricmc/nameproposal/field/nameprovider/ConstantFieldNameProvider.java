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

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.fabricmc.nameproposal.field.FieldData;

/**
 * A field name provider that returns a given name.
 *
 * <p>This provider can be used as the delegate of a conditional provider
 * to restrict the name to certain fields matching a predicate.
 */
public record ConstantFieldNameProvider(String name) implements FieldNameProvider {
	protected static final Codec<ConstantFieldNameProvider> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
			Codec.STRING.fieldOf("name").forGetter(ConstantFieldNameProvider::name)
		).apply(instance, ConstantFieldNameProvider::new);
	});

	@Override
	public String getName(FieldData field) {
		return this.name;
	}

	@Override
	public Codec<ConstantFieldNameProvider> getCodec() {
		return CODEC;
	}

	@Override
	public String toString() {
		return "Constant[" + this.name + "]";
	}
}
