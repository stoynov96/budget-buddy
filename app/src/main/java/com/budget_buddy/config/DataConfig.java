package com.budget_buddy.config;

import com.budget_buddy.exception.InvalidDataLabelException;

public class DataConfig {
    // todo: This is awkward. It for now but we should probably refine it
    public static class DataLabels {
        public static final String USERS =          "Users";
        public static final String TEST_NESTED =    "Nested";

        /**
         * Verifies that the label provided is a part of this configuration
         * @param label Label to verify
         * @throws InvalidDataLabelException Thrown if the label is invalid
         */
        public static void VerifyLabel(String label)
                throws InvalidDataLabelException {
            switch(label) {
                case USERS: break;
                case TEST_NESTED: break;
                default: throw new InvalidDataLabelException("Invalid Data Label");
            }
        }
    }
}
