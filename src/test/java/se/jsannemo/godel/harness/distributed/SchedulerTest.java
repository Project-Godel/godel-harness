package se.jsannemo.godel.harness.distributed;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth8.assertThat;

public class SchedulerTest {

  @Test
  public void testNoMessages() {
    Set<Integer> schedule = new HashSet<>();
    Scheduler s = new Scheduler(5);
    while (true) {
      Optional<Integer> scheduled = s.schedule();
      if (scheduled.isPresent()) {
        assertThat(schedule.add(scheduled.get())).isTrue();
      } else {
        break;
      }
    }
    assertThat(schedule).containsExactly(0, 1, 2, 3, 4);
  }

  @Test
  public void testMessages() {
    Scheduler s = new Scheduler(2);
    int first = s.schedule().get();
    int second = 1 - first;
    assertThat(s.receive(first, second)).isFalse();
    assertThat(s.blockedFor(first)).isEqualTo(second);

    assertThat(s.schedule()).hasValue(second);
    assertThat(s.schedule()).isEmpty();
    s.send(new Scheduler.Message(5, second, first, 1));
    s.send(new Scheduler.Message(5, second, first, 2));
    assertThat(s.receive(second, first)).isFalse();
    assertThat(s.blockedFor(second)).isEqualTo(first);

    assertThat(s.schedule()).hasValue(first);
    assertThat(s.nextMessage(first, second).message).isEqualTo(1);
    assertThat(s.receive(first, second)).isTrue();
    assertThat(s.nextMessage(first, second).message).isEqualTo(2);
    assertThat(s.receive(first, second)).isFalse();
  }

  @Test
  public void testMessageAny() {
    Scheduler s = new Scheduler(2);
    int first = s.schedule().get();
    int second = 1 - first;
    s.send(new Scheduler.Message(5, first, second, 1));
    s.send(new Scheduler.Message(8, first, first, 2));
    assertThat(s.receive(first, Scheduler.WAITING_ANY)).isFalse();
    assertThat(s.blockedFor(first)).isEqualTo(Scheduler.WAITING_ANY);

    assertThat(s.schedule()).hasValue(second);
    s.send(new Scheduler.Message(6, second, first, 3));
    s.send(new Scheduler.Message(7, second, second, 4));
    assertThat(s.receive(second, Scheduler.WAITING_ANY)).isFalse();
    assertThat(s.blockedFor(second)).isEqualTo(Scheduler.WAITING_ANY);

    assertThat(s.schedule()).hasValue(second);
    assertThat(s.nextMessage(second, Scheduler.WAITING_ANY).message).isEqualTo(1);
    assertThat(s.receive(second, Scheduler.WAITING_ANY)).isFalse();

    assertThat(s.schedule()).hasValue(first);
    assertThat(s.nextMessage(first, Scheduler.WAITING_ANY).message).isEqualTo(3);
    assertThat(s.receive(first, Scheduler.WAITING_ANY)).isFalse();

    assertThat(s.schedule()).hasValue(second);
    assertThat(s.nextMessage(second, Scheduler.WAITING_ANY).message).isEqualTo(4);
    assertThat(s.receive(second, Scheduler.WAITING_ANY)).isFalse();

    assertThat(s.schedule()).hasValue(first);
    assertThat(s.nextMessage(first, Scheduler.WAITING_ANY).message).isEqualTo(2);
    assertThat(s.receive(first, Scheduler.WAITING_ANY)).isFalse();
  }
}
