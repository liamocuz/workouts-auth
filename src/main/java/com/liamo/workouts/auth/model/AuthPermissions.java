package com.liamo.workouts.auth.model;

/**
 * Represents the permissions a User can have in our system.
 */
public class AuthPermissions {
    public enum Workouts {
        READ,
        WRITE,
        DELETE;

        public String getPermission() {
            return "workouts." + this.name().toLowerCase();
        }
    }

    // TODO: Make more fine-grained permissions for Users
    public enum Users {
        MANAGE;

        public String getPermission() {
            return "users." + this.name().toLowerCase();
        }
    }
}
