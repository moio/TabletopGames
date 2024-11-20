package games.totoro.metrics;

import core.actions.LogEvent;
import core.interfaces.IGameEvent;
import evaluation.listeners.MetricsGameListener;
import evaluation.metrics.AbstractMetric;
import evaluation.metrics.Event;
import evaluation.metrics.IMetricsCollection;

import java.util.*;

import static java.util.Collections.singleton;


@SuppressWarnings("unused")
public class TotoroMetrics implements IMetricsCollection {

    public enum TotoroEvent implements IGameEvent {
        CardPlayed;

        @Override
        public Set<IGameEvent> getValues() {
            return new HashSet<>(Arrays.asList(TotoroMetrics.TotoroEvent.values()));
        }
    }

    /**
     * How many times a card type is played during the game
     */
    public static class CardPlayedCount extends AbstractMetric {

        @Override
        public Map<String, Class<?>> getColumns(int nPlayersPerGame, Set<String> playerNames) {
            Map<String, Class<?>> columns = new HashMap<>();
            columns.put("Card played", String.class);
            return columns;
        }

        @Override
        protected boolean _run(MetricsGameListener listener, Event e, Map<String, Object> records) {
            var l = (LogEvent) e.action;
            records.put("Card played", l.text);
            return true;
        }

        @Override
        public Set<IGameEvent> getDefaultEventTypes() {
            return singleton(TotoroEvent.CardPlayed);
        }
    }
}
