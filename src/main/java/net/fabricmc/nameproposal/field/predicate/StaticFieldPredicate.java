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

public class StaticFieldPredicate extends FieldPredicate {
	protected static final Codec<StaticFieldPredicate> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
			Codec.BOOL.fieldOf("static").forGetter(predicate -> predicate.isStatic)
		).apply(instance, StaticFieldPredicate::new);
	});

	private final boolean isStatic;

	public StaticFieldPredicate(boolean isStatic) {
		this.isStatic = isStatic;
	}

	@Override
	public boolean test(FieldData field) {
		return field.isStatic() == this.isStatic;
	}

	@Override
	protected Codec<StaticFieldPredicate> getCodec() {
		return CODEC;
	}

	@Override
	public String toString() {
		return "Static = " + this.isStatic;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof StaticFieldPredicate staticPredicate)) return false;

		return this.isStatic == staticPredicate.isStatic;
	}

	@Override
	public int hashCode() {
		return this.isStatic ? 1 : 0;
	}
}
