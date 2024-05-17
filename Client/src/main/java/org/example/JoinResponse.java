package org.example;

import java.io.Serializable;

public class JoinResponse extends Message implements Serializable {
        private int clock;

        public JoinResponse(int clock) {
            super();
            this.clock = clock;
        }
        public int getClock() {
            return clock;
    }
}
