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

package net.fabricmc.nameproposal;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.fabricmc.nameproposal.field.nameprovider.FieldNameProvider;
import net.fabricmc.nameproposal.field.nameprovider.FieldNameProviders;
import net.fabricmc.nameproposal.registry.Codecs;

public record NameProposalConfig(FieldNameProvider fieldNameProvider, int version) {
	public static final String SYSTEM_PROPERTY_KEY = "name_proposal_config";

	public static final Codec<NameProposalConfig> CODEC = Codecs.validate(RecordCodecBuilder.create(instance -> {
		return instance.group(
			FieldNameProviders.CODEC.fieldOf("field_name_provider").forGetter(NameProposalConfig::fieldNameProvider),
			Codec.INT.fieldOf("version").forGetter(NameProposalConfig::version)
		).apply(instance, NameProposalConfig::new);
	}), config -> {
		if (config.version() == 1) {
			return null;
		}

		return "Unsupported version found for name proposal config! (found version " + config.version + ", version 1 supported)";
	});
}
