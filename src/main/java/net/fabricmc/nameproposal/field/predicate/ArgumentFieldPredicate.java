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

import java.util.Objects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.fabricmc.nameproposal.field.FieldData;

public class ArgumentFieldPredicate extends StringFieldPredicate {
	protected static final Codec<ArgumentFieldPredicate> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
			Codec.STRING.fieldOf("value").forGetter(predicate -> predicate.value),
			Codec.INT.fieldOf("index").forGetter(predicate -> predicate.index)
		).apply(instance, ArgumentFieldPredicate::new);
	});

	private final int index;

	public ArgumentFieldPredicate(String value, int index) {
		super(value);
		this.index = index;
	}

	@Override
	protected String getActualValue(FieldData field) {
		Object[] args = field.args();
		int index = this.index < 0 ? args.length - 1 - this.index : this.index;

		if (index < 0 || index >= args.length) {
			return null;
		} else if (field.args()[index] instanceof String string) {
			return string;
		}

		return null;
	}

	@Override
	public Codec<ArgumentFieldPredicate> getCodec() {
		return CODEC;
	}

	@Override
	public String toString() {
		return "Argument " + this.index + " = " + this.value;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ArgumentFieldPredicate argument)) return false;

		return this.index == argument.index && Objects.equals(this.value, argument.value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.index, this.value);
	}
}
