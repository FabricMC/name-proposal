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

import java.util.Objects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.fabricmc.nameproposal.field.FieldData;

public class ConstantFieldNameProvider extends FieldNameProvider {
	protected static final Codec<ConstantFieldNameProvider> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
			Codec.STRING.fieldOf("name").forGetter(nameProvider -> nameProvider.name)
		).apply(instance, ConstantFieldNameProvider::new);
	});

	protected final String name;

	public ConstantFieldNameProvider(String name) {
		this.name = name;
	}

	@Override
	public String getName(FieldData field) {
		return this.name;
	}

	@Override
	protected Codec<ConstantFieldNameProvider> getCodec() {
		return CODEC;
	}

	@Override
	public String toString() {
		return "Constant[" + this.name + "]";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ConstantFieldNameProvider constant)) return false;
		return Objects.equals(this.name, constant.name);
	}

	@Override
	public int hashCode() {
		return this.name.hashCode();
	}
}
