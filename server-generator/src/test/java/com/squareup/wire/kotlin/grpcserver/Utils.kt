/*
 * Copyright (C) 2024 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.squareup.wire.kotlin.grpcserver

import assertk.Assert
import assertk.assertions.containsExactlyInAnyOrder
import com.squareup.wire.SchemaBuilder
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.buffer
import okio.source
import java.io.File

internal fun FileSystem.findFiles(path: String): Iterable<String> {
    return listRecursively(path.withPlatformSlashes().toPath())
        .filter { !metadata(it).isDirectory }
        .map { it.toString() }
        .toSet()
}

/**
 * This asserts that [this] contains exactly in any order all [values] regardless of the slash they
 * may contain. This is useful to write one assertion which can be run on both macOS and Windows.
 */
internal fun Assert<Iterable<String>>.containsExactlyInAnyOrderAsRelativePaths(vararg values: String) {
    @Suppress("NAME_SHADOWING")
    val values = values.map { it.withPlatformSlashes() }
    return containsExactlyInAnyOrder(*values.toTypedArray())
}

private val slash = Path.DIRECTORY_SEPARATOR
private val otherSlash = if (slash == "/") "\\" else "/"

/**
 * This returns a string where all other slashes are replaced with the slash of the local platform.
 * On Windows, `/` will be replaced with `\`. On other platforms, `\` will be replaced with `/`.
 */
internal fun String.withPlatformSlashes(): String {
    return replace(otherSlash, slash)
}

internal fun SchemaBuilder.addLocal(path: Path): SchemaBuilder {
    val file = File(path.toString())
    file.source().use { source ->
        val protoFile = source.buffer().readUtf8()
        return add(path, protoFile)
    }
}
