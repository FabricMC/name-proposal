/*
 * Copyright (c) 2016, 2021 FabricMC
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

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.ClassReader;

public class RecordTestMain {
	public static void main(String... args) throws Throwable {
		var url = RecordTestMain.class.getResource("./TestRecord.class");
		var bytes = Files.readAllBytes(Path.of(url.toURI()));

		var reader = new ClassReader(bytes);
		Map<String, String> records = new HashMap<>();
		reader.accept(new RecordComponentNameFinder(Constants.ASM_VERSION, records), 0);

		Map<String, String> expected = Map.ofEntries(
				Map.entry("a", "a"),
				Map.entry("another", "another"),
				Map.entry("data", "data"),
				Map.entry("aBitOfLongName", "aBitOfLongName")
		);

		if (!expected.equals(records)) {
			System.out.println("failed, found: " + records);
		} else {
			System.out.println("passed");
		}
	}
}
