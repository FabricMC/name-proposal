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

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.fabricmc.nameproposal.field.FieldData;

public class NameFieldPredicate extends StringFieldPredicate {
	protected static final Codec<NameFieldPredicate> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
			Codec.STRING.fieldOf("value").forGetter(predicate -> predicate.value)
		).apply(instance, NameFieldPredicate::new);
	});

	public NameFieldPredicate(String value) {
		super(value);
	}

	@Override
	protected String getActualValue(FieldData field) {
		return field.name();
	}

	@Override
	public Codec<NameFieldPredicate> getCodec() {
		return CODEC;
	}

	@Override
	public String toString() {
		return "Name = " + this.value;
	}
}
