package se.jsannemo.godel.harness.distributed;

import org.junit.jupiter.api.Test;
import se.jsannemo.godel.harness.Stats;

import static com.google.common.truth.Truth.assertThat;

public class StatsTest {

  @Test
  public void testInitial() {
    Stats s = new Stats();
    assertThat(s.getInstructions()).isEqualTo(0);
    assertThat(s.getMaxMemory()).isEqualTo(0);
  }

  @Test
  public void testStats() {
    Stats s = new Stats(1, 2);
    assertThat(s.getInstructions()).isEqualTo(1);
    assertThat(s.getMaxMemory()).isEqualTo(2);

    s = s.max(new Stats(0, 0));
    assertThat(s.getInstructions()).isEqualTo(1);
    assertThat(s.getMaxMemory()).isEqualTo(2);

    s = s.max(new Stats(0, 3));
    assertThat(s.getInstructions()).isEqualTo(1);
    assertThat(s.getMaxMemory()).isEqualTo(3);

    // Check immutability.
    s.max(new Stats(100, 100));
    assertThat(s.getInstructions()).isEqualTo(1);
    assertThat(s.getMaxMemory()).isEqualTo(3);
  }
}
