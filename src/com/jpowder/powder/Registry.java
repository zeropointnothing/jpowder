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
    private final ArrayList<RelationshipEntry> registeredRelationships;

    /**
     * Registry entry, used for storing BasePowder objects and an ID.
     */
    private static class RegistryEntry {
        public final BasePowder powder;
        public final String id;

        public RegistryEntry(BasePowder powder, String id) {
            this.powder = powder;
            this.id = id;
        }
    }
    public static class RelationshipEntry {
        public final String first;
        public final String second;
        public final String out;
        public final RelationshipType relationshipType;

        public RelationshipEntry(String first, String second, String out, RelationshipType relationshipType) {
            this.first = first;
            this.second = second;
            this.out = out;
            this.relationshipType = relationshipType;
        }
    }

    public Registry() {
        registeredPowder = new ArrayList<>();
        registeredRelationships = new ArrayList<>();
    }


    /**
     * Register a new powder.
     * @param powder The instance of the powder to clone
     * @param id The id to set the powder to
     */
    public void register(BasePowder powder, String id) {
        registeredPowder.add(new RegistryEntry(powder, id));
    }
    public void registerRelationship(String first, String second, String out, RelationshipType relationshipType) throws IllegalArgumentException {
        if (isRegistered(first) && isRegistered(out) && isRegistered(out)) {
            registeredRelationships.add(new RelationshipEntry(first, second, out, relationshipType));
        } else {
            throw new IllegalArgumentException("Invalid ID for powder!");
        }
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
                BasePowder powderClone = entry.powder.clone();
                return entry.powder.clone();
            }
        }

        throw new RuntimeException("No such Powder: " + id);
    }
    public RelationshipEntry getRelationship(String first, String second) {
        for (RelationshipEntry entry : registeredRelationships) {
            if (Objects.equals(entry.first, first) && Objects.equals(entry.second, second) ||
                    Objects.equals(entry.first, second) && Objects.equals(entry.second, first)) {
                return entry;
            }
        }
        throw new IllegalArgumentException("No such relationship (" + first + "," + second + ") is registered.");
    }
    public String getID(BasePowder powder) throws IllegalArgumentException {
        for (RegistryEntry entry : registeredPowder) {
            if (entry.powder.getClass() == powder.getClass()) {
                return entry.id;
            }
        }
        throw new IllegalArgumentException("No such class '" + powder.getClass() + "' is registered.");
    }
    public boolean hasRelationship(String first, String second) {
        for (RelationshipEntry entry : registeredRelationships) {
            if (Objects.equals(entry.first, first) && Objects.equals(entry.second, second) ||
                    Objects.equals(entry.first, second) && Objects.equals(entry.second, first)) {
                return true;
            }
        }
        return false;
    }
    public boolean isRegistered(String id) {
        for (RegistryEntry entry : registeredPowder) {
            if (Objects.equals(entry.id, id)) {
                return true;
            }
        }

        return false;
    }
}
