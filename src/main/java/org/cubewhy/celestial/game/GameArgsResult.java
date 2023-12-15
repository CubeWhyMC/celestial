package org.cubewhy.celestial.game;

import java.io.File;
import java.util.List;

public record GameArgsResult(List<String> args, File natives) {
}
