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
import net.fabricmc.nameproposal.field.predicate.FieldPredicate;
import net.fabricmc.nameproposal.field.predicate.FieldPredicates;
import net.fabricmc.nameproposal.registry.Codecs;

/**
 * A field name provider that delegates to another provider depending on
 * whether the field matches all of the given predicates.
 *
 * <p>If any of the predicates are not matched, {@code null} is returned.
 */
public record ConditionalFieldNameProvider(FieldNameProvider delegate, List<FieldPredicate> conditions) implements FieldNameProvider {
	protected static final Codec<ConditionalFieldNameProvider> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
			FieldNameProviders.CODEC.fieldOf("delegate").forGetter(ConditionalFieldNameProvider::delegate),
			Codecs.listOrUnit(FieldPredicates.CODEC).fieldOf("conditions").forGetter(ConditionalFieldNameProvider::conditions)
		).apply(instance, ConditionalFieldNameProvider::new);
	});

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
	public Codec<ConditionalFieldNameProvider> getCodec() {
		return CODEC;
	}

	@Override
	public String toString() {
		return this.delegate + " if " + this.conditions;
	}
}
