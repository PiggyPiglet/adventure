/*
 * This file is part of adventure, licensed under the MIT License.
 *
 * Copyright (c) 2017-2020 KyoriPowered
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package net.kyori.adventure.text;

import com.google.common.collect.ImmutableSet;
import java.util.Collections;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.junit.jupiter.api.Test;

import static net.kyori.adventure.text.TextAssertions.assertDecorations;
import static net.kyori.test.WeirdAssertions.assertAllEqualToEachOther;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TextComponentTest extends AbstractComponentTest<TextComponent, TextComponent.Builder> {
  @Override
  TextComponent.Builder builder() {
    return TextComponent.builder("foo");
  }

  @Test
  void testOfChildren() {
    assertSame(TextComponent.empty(), TextComponent.ofChildren()); // empty array
    assertEquals(
      TextComponent.builder()
        .append(TextComponent.of("a"))
        .append(TextComponent.builder("b"))
        .build(),
      TextComponent.ofChildren(
        TextComponent.of("a"),
        TextComponent.builder("b")
      )
    );
  }

  @Test
  void testOf() {
    final TextComponent component = TextComponent.of("foo");
    assertEquals("foo", component.content());
    assertNull(component.color());
    assertDecorations(component, ImmutableSet.of(), ImmutableSet.of());
  }

  @Test
  void testOfSameResult() {
    assertAllEqualToEachOther(
      TextComponent.of("foo", Style.of(TextColor.of(0x0a1ab9))),
      TextComponent.of("foo", TextColor.of(0x0a1ab9)),
      TextComponent.of("foo", TextColor.of(0x0a1ab9), ImmutableSet.<TextDecoration>of())
    );
    assertAllEqualToEachOther(
      TextComponent.of("foo", Style.of(TextColor.of(0x0a1ab9), TextDecoration.BOLD)),
      TextComponent.of("foo", TextColor.of(0x0a1ab9), TextDecoration.BOLD),
      TextComponent.of("foo", TextColor.of(0x0a1ab9), ImmutableSet.of(TextDecoration.BOLD))
    );
  }

  @Test
  void testOfKnownChar() {
    assertSame(TextComponent.newline(), TextComponent.of('\n'));
    assertSame(TextComponent.space(), TextComponent.of(' '));
  }

  @Test
  void testOf_color() {
    final TextComponent component = TextComponent.of("foo", NamedTextColor.GREEN);
    assertEquals("foo", component.content());
    assertEquals(NamedTextColor.GREEN, component.color());
    assertDecorations(component, ImmutableSet.of(), ImmutableSet.of());
  }

  @Test
  void testOf_color_decorations() {
    final TextComponent component = TextComponent.of("foo", NamedTextColor.GREEN, ImmutableSet.of(TextDecoration.BOLD));
    assertEquals("foo", component.content());
    assertEquals(NamedTextColor.GREEN, component.color());
    assertDecorations(component, ImmutableSet.of(TextDecoration.BOLD), ImmutableSet.of());
  }

  @Test
  void testMake() {
    final TextComponent component = TextComponent.make(builder -> {
      builder.content("foo");
      builder.color(NamedTextColor.DARK_PURPLE);
    });
    assertEquals("foo", component.content());
    assertEquals(NamedTextColor.DARK_PURPLE, component.color());
  }

  @Test
  void testContains() {
    final Component child = TextComponent.of("kittens");
    final Component component = TextComponent.builder()
      .content("cat")
      .append(child)
      .build();
    assertTrue(component.contains(child));
  }

  @Test
  void testContent() {
    final TextComponent c0 = TextComponent.of("foo");
    final TextComponent c1 = c0.content("bar");
    assertEquals("foo", c0.content());
    assertEquals("bar", c1.content());
  }

  @Test
  void testReplace() {
    final TextComponent component = TextComponent.builder()
      .content("cat says ")
      .append(TranslatableComponent.of("cat.meow")) // or any non-text component
      .build();
    final TextComponent replaced = component.replace(Pattern.compile("says"), match -> match.color(NamedTextColor.DARK_PURPLE));
    assertEquals(TextComponent.builder()
      .content("cat ")
      .append("says", NamedTextColor.DARK_PURPLE)
      .append(" ")
      .append(TranslatableComponent.of("cat.meow"))
      .build(), replaced);
  }

  @Test
  void testReplaceFirst() {
    final TextComponent component = TextComponent.builder()
      .content("Buffalo buffalo Buffalo buffalo buffalo buffalo Buffalo buffalo ")
      .append(TranslatableComponent.of("buffalo.buffalo")) // or any non-text component
      .build();
    final TextComponent replaced = component.replaceFirst(Pattern.compile("buffalo"), match -> match.color(NamedTextColor.DARK_PURPLE));
    assertEquals(TextComponent.builder()
      .content("Buffalo ")
      .append("buffalo", NamedTextColor.DARK_PURPLE)
      .append(" Buffalo buffalo buffalo buffalo Buffalo buffalo ")
      .append(TranslatableComponent.of("buffalo.buffalo"))
      .build(), replaced);
  }

  @Test
  void testReplaceN() {
    final TextComponent component = TextComponent.builder()
      .content("Buffalo buffalo Buffalo buffalo buffalo buffalo Buffalo buffalo ")
      .append(TranslatableComponent.of("buffalo.buffalo")) // or any non-text component
      .build();
    final TextComponent replaced = component.replace(Pattern.compile("buffalo"), match -> match.color(NamedTextColor.DARK_PURPLE), 2);
    assertEquals(TextComponent.builder()
      .content("Buffalo ")
      .append("buffalo", NamedTextColor.DARK_PURPLE)
      .append(" Buffalo ")
      .append("buffalo", NamedTextColor.DARK_PURPLE)
      .append(" buffalo buffalo Buffalo buffalo ")
      .append(TranslatableComponent.of("buffalo.buffalo"))
      .build(), replaced);
  }

  @Test
  void testReplaceEveryOther() {
    final TextComponent component = TextComponent.builder()
      .content("purple purple purple purple purple purple purple purple ")
      .append(TranslatableComponent.of("purple.purple")) // or any non-text component
      .build();

    final TextComponent replaced = component.replace(Pattern.compile("purple"), match -> match.color(NamedTextColor.DARK_PURPLE), (index, replace) -> index % 2 == 0 ? TextComponent.PatternReplacementResult.REPLACE : TextComponent.PatternReplacementResult.CONTINUE);

    assertEquals(TextComponent.builder()
      .content("purple ")
      .append("purple", NamedTextColor.DARK_PURPLE)
      .append(" purple ")
      .append("purple", NamedTextColor.DARK_PURPLE)
      .append(" purple ")
      .append("purple", NamedTextColor.DARK_PURPLE)
      .append(" purple ")
      .append("purple", NamedTextColor.DARK_PURPLE)
      .append(" ")
      .append(TranslatableComponent.of("purple.purple"))
      .build(), replaced);
  }

  @Test
  void testBuildEmptyIsEmpty() {
    assertSame(TextComponent.empty(), TextComponent.builder().build());
  }

  @Test
  void testJoin() {
    assertEquals(TextComponent.empty(), TextComponent.join(TextComponent.space(), Collections.emptyList()));

    final Component c0 = TextComponent.join(
      TextComponent.space(),
      IntStream.range(0, 3)
        .mapToObj(TextComponent::of)
        .toArray(Component[]::new)
    );
    assertEquals(
      TextComponent.builder()
        .append(TextComponent.of(0))
        .append(TextComponent.space())
        .append(TextComponent.of(1))
        .append(TextComponent.space())
        .append(TextComponent.of(2))
        .build(),
      c0
    );
  }
}
