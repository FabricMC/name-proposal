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

public class EnumFieldPredicate extends FieldPredicate {
	protected static final Codec<EnumFieldPredicate> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
			Codec.BOOL.fieldOf("enum").forGetter(predicate -> predicate.isEnum)
		).apply(instance, EnumFieldPredicate::new);
	});

	private final boolean isEnum;

	public EnumFieldPredicate(boolean isEnum) {
		this.isEnum = isEnum;
	}

	@Override
	public boolean test(FieldData field) {
		return field.isEnum() == this.isEnum;
	}

	@Override
	protected Codec<EnumFieldPredicate> getCodec() {
		return CODEC;
	}

	@Override
	public String toString() {
		return "Enum = " + this.isEnum;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof EnumFieldPredicate enumPredicate)) return false;

		return this.isEnum == enumPredicate.isEnum;
	}

	@Override
	public int hashCode() {
		return this.isEnum ? 1 : 0;
	}
}
