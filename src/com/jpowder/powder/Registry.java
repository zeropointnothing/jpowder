package com.jpowder.powder;

import java.util.ArrayList;
import java.util.Objects;

public class Registry {
    private ArrayList<RegistryEntry> registeredPowder;

        /**
         * Registry entry, used for storing BasePowder objects and an ID.
         */
        private record RegistryEntry(BasePowder powder, String id) {
    }

    public Registry() {
        registeredPowder = new ArrayList<RegistryEntry>();
    }
    public void register(BasePowder powder, String id) {
        registeredPowder.add(new RegistryEntry(powder, id));
    }
    public BasePowder createInstance(String id) {
        for (RegistryEntry entry : registeredPowder) {
            if (Objects.equals(entry.id, id)) {
                return entry.powder.clone();
            }
        }

        throw new RuntimeException("No such Powder: " + id);
    }
}
