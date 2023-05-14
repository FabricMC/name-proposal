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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;

import net.fabricmc.nameproposal.field.nameprovider.ConditionalFieldNameProvider;
import net.fabricmc.nameproposal.field.nameprovider.ConstantFieldNameProvider;
import net.fabricmc.nameproposal.field.nameprovider.FieldNameProvider;
import net.fabricmc.nameproposal.field.nameprovider.FieldNameProviders;
import net.fabricmc.nameproposal.field.nameprovider.SequenceFieldNameProvider;
import net.fabricmc.nameproposal.field.nameprovider.StringArgumentFieldNameProvider;
import net.fabricmc.nameproposal.field.predicate.DescriptorFieldPredicate;
import net.fabricmc.nameproposal.field.predicate.InternalInitFieldPredicate;
import net.fabricmc.nameproposal.field.predicate.StaticFieldPredicate;
import net.fabricmc.nameproposal.registry.Registry;

public class FieldTestMain {
	static {
		Registry.init();
	}

	private static final FieldNameProvider EXPECTED_NAME_PROVIDER = new SequenceFieldNameProvider(
		new ConditionalFieldNameProvider(new ConstantFieldNameProvider("CODEC"), new StaticFieldPredicate(true), new DescriptorFieldPredicate("Lcom/mojang/serialization/Codec;")),
		new ConditionalFieldNameProvider(StringArgumentFieldNameProvider.INSTANCE, new StaticFieldPredicate(true), InternalInitFieldPredicate.INSTANCE)
	);

	@Test
	public void parseConfig() throws Throwable {
		var url = FieldTestMain.class.getClassLoader().getResource(NameProposalConfig.FILE_NAME);

		try (var reader = new FileReader(new File(url.toURI()))) {
			var tree = JsonParser.parseReader(reader);

			var result = FieldNameProviders.CODEC.parse(JsonOps.INSTANCE, tree);
			var nameProvider = result.getOrThrow(false, s -> { });

			assertEquals(EXPECTED_NAME_PROVIDER, nameProvider);
		}
	}

	@Test
	public void fieldNames() throws Throwable {
		var url = FieldTestMain.class.getResource("./TestClass.class");
		var bytes = Files.readAllBytes(Path.of(url.toURI()));

		Map<MappingEntry, String> names = new FieldNameFinder().findNames(List.of(bytes), EXPECTED_NAME_PROVIDER);

		Map<MappingEntry, String> expected = Map.ofEntries(
				Map.entry(new MappingEntry("net/fabricmc/nameproposal/TestClass", "XYZ", "Lnet/fabricmc/nameproposal/TestClass;"), "XYZ"),
				Map.entry(new MappingEntry("net/fabricmc/nameproposal/TestClass", "field_2", "Lcom/mojang/serialization/Codec;"), "CODEC"),
				Map.entry(new MappingEntry("net/fabricmc/nameproposal/TestClass", "field_1", "Ljava/lang/String;"), "ABC")
		);

		System.out.println("\n\nExpected names: ");
		printMap(expected);

		System.out.println("\n\nFound names: ");
		printMap(names);

		assertEquals(expected, names);
	}

	public static void printMap(Map<MappingEntry, String> map) {
		map.forEach((k, v) -> {
			System.out.println(" - " + k + " --> " + v);
		});
	}
}
