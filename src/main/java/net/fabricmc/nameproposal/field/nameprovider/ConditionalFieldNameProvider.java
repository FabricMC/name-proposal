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
import java.util.Objects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.fabricmc.nameproposal.field.FieldData;
import net.fabricmc.nameproposal.field.predicate.FieldPredicate;
import net.fabricmc.nameproposal.field.predicate.FieldPredicates;
import net.fabricmc.nameproposal.registry.Codecs;

public class ConditionalFieldNameProvider extends FieldNameProvider {
	protected static final Codec<ConditionalFieldNameProvider> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
			FieldNameProviders.CODEC.fieldOf("delegate").forGetter(nameProvider -> nameProvider.delegate),
			Codecs.listOrUnit(FieldPredicates.CODEC).fieldOf("conditions").forGetter(nameProvider -> nameProvider.conditions)
		).apply(instance, ConditionalFieldNameProvider::new);
	});

	private final FieldNameProvider delegate;
	private final List<FieldPredicate> conditions;

	public ConditionalFieldNameProvider(FieldNameProvider delegate, List<FieldPredicate> conditions) {
		this.delegate = Objects.requireNonNull(delegate);
		this.conditions = List.copyOf(conditions);
	}

	public ConditionalFieldNameProvider(FieldNameProvider delegate, FieldPredicate... conditions) {
		this.delegate = Objects.requireNonNull(delegate);
		this.conditions = List.of(conditions);
	}

	@Override
	public String getName(FieldData field) {
		for (var condition : this.conditions) {
			if (!condition.test(field)) {
				return null;
			}
		}

		return this.delegate.getName(field);
	}

	@Override
	protected Codec<ConditionalFieldNameProvider> getCodec() {
		return CODEC;
	}

	@Override
	public String toString() {
		return this.delegate + " if " + this.conditions;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ConditionalFieldNameProvider conditional)) return false;

		return Objects.equals(this.delegate, conditional.delegate) && Objects.equals(this.conditions, conditional.conditions);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.delegate, this.conditions);
	}
}
