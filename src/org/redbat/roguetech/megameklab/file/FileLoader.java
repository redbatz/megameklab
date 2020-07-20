package org.redbat.roguetech.megameklab.file;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FileLoader {

    public static <T> List<T> loadFiles(Path directory, Predicate<Path> filter, Function<Path, T> loadFunction) {
        log.debug("Loading files from path {}", directory.toString());
        try (Stream<Path> files = Files.walk(directory)) {
            return files.filter(filter).map(loadFunction).collect(Collectors.toList());
        } catch (IOException e) {
            log.error("Unable to load files from directory {}", directory.toString(), e);
            return Collections.emptyList();
        }
    }
}
