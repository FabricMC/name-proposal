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

import net.fabricmc.nameproposal.field.FieldData;

public class InternalInitFieldPredicate implements FieldPredicate {
	public static final InternalInitFieldPredicate INSTANCE = new InternalInitFieldPredicate();
	protected static final Codec<InternalInitFieldPredicate> CODEC = Codec.unit(INSTANCE);

	private InternalInitFieldPredicate() {
		return;
	}

	@Override
	public boolean test(FieldData field) {
		return field.owner().equals(field.methodOwner());
	}

	@Override
	public Codec<InternalInitFieldPredicate> getCodec() {
		return CODEC;
	}

	@Override
	public String toString() {
		return "Internal Init";
	}
}
