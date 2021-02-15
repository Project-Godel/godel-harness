package se.jsannemo.godel.harness.distributed;

import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

public class ClocksTest {

  @Test
  public void testInitial() {
    Clocks c = Clocks.create(3);
    for (int i = 0; i < 3; i++) {
      assertThat(c.time(i)).isEqualTo(0);
    }
  }

  @Test
  public void testClocks() {
    Clocks c = Clocks.create(2);
    c.tick(0, 5);
    c.causality(1, 5);
    assertThat(c.time(1)).isEqualTo(5);
    c.tick(1, 1);
    assertThat(c.time(1)).isEqualTo(6);
    assertThat(c.time(0)).isEqualTo(5);
  }
}
