package com.jpowder.powder;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Powder Registry.
 * <p/>
 * Holds registered powders so that they can be cloned later.
 */
public class Registry {
    private final ArrayList<RegistryEntry> registeredPowder;

        /**
         * Registry entry, used for storing BasePowder objects and an ID.
         */
        private record RegistryEntry(BasePowder powder, String id) {
    }

    public Registry() {
        registeredPowder = new ArrayList<>();
    }


    /**
     * Register a new powder.
     * @param powder The instance of the powder to clone
     * @param id The id to set the powder to
     */
    public void register(BasePowder powder, String id) {
        registeredPowder.add(new RegistryEntry(powder, id));
    }

    /**
     * Create a new instance of a powder.
     * @param id The id of the powder to create
     * @return a clone of the powder
     * @throws RuntimeException if the requested powder hasn't been registered
     */
    public BasePowder createInstance(String id) throws RuntimeException {
        for (RegistryEntry entry : registeredPowder) {
            if (Objects.equals(entry.id, id)) {
                return entry.powder.clone();
            }
        }

        throw new RuntimeException("No such Powder: " + id);
    }
}
