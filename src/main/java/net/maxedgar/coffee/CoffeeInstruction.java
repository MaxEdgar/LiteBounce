/*
 * This file is part of Coffee (https://github.com/MaxEdgar/Coffee)
 *
 * Copyright (c) 2015 - 2026 CCBlueX
 *
 * Coffee is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Coffee is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Coffee. If not, see <https://www.gnu.org/licenses/>.
 */
package net.maxedgar.coffee;

import net.maxedgar.coffee.utils.client.GitInfo;
import org.jspecify.annotations.NullMarked;

import javax.swing.*;
import java.awt.*;
import java.net.URI;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * This class is for manual open of the jar file.
 * It should not use any other external libraries such as Kotlin stdlib, JavaFX or Minecraft
 * because they are not included in the jar.
 */
@NullMarked
public final class LiquidInstruction {

  private LiquidInstruction() {}

  private static Stream<ImageIcon> loadIcons() {
    return Stream.of(
            "/resources/coffee/icon_64x64.png",
            "/resources/coffee/icon_32x32.png",
            "/resources/coffee/icon_16x16.png"
        ).map(LiquidInstruction.class::getResource)
        .filter(Objects::nonNull)
        .map(ImageIcon::new);
  }

  private static boolean browse(URI uri) {
    try {
      Desktop.getDesktop().browse(uri);
      return true;
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  public static void main(String[] args) {
    var buttons = new String[]{"Manual installation guide", "Download LiquidLauncher", "Open GitHub", "Open Discord", "Close"};

    var result = JOptionPane.showOptionDialog(
        null,
        """
            Welcome to %s!

            This file is a Fabric mod, you should use it with a launcher and Fabric.

            You can click the button below to open the manual installation guide, or download the LiquidLauncher.
            If you need help, you can join our Discord server for support.
            If you found any bugs or have any feature requests, please report them on our GitHub repository.
            """.formatted(Coffee.CLIENT_NAME),
        Coffee.CLIENT_NAME + " by " + Coffee.CLIENT_AUTHOR + " (" + GitInfo.version() + ")",
        JOptionPane.DEFAULT_OPTION,
        JOptionPane.INFORMATION_MESSAGE,
        loadIcons().findFirst().orElse(null),
        buttons,
        buttons[0]
    );

    switch (result) {
      case 0 -> browse(URI.create("https://github.com/MaxEdgar/CoffeeV2/docs/get-started/manual-installation"));
      case 1 -> browse(URI.create("https://github.com/MaxEdgar/CoffeeV2/download"));
      case 2 -> browse(URI.create("https://github.com/MaxEdgar/Coffee"));
      case 3 -> browse(URI.create("https://github.com/MaxEdgar/CoffeeV2/discord"));
      default -> System.exit(0);
    }
  }

}
