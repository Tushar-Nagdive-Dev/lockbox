package org.inn.lockbox.components;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class LockboxIconStore {

    private final Map<String, String> registry = new HashMap<>();

    public LockboxIconStore() {
        // --- VAULT STATUS ---
        registry.put("vault.locked", "🔒");
        registry.put("vault.open", "🔓");
        registry.put("vault.empty", "🫙");
        // --- DATA TYPES (The "What") ---
        registry.put("type.login",   "🔐");
        registry.put("type.bank",    "💳");
        registry.put("type.api",     "🛠️");
        registry.put("type.note",    "📝");
        registry.put("type.server",  "🖥️");
        // --- SYSTEM ACTIONS (The "How") ---
        registry.put("action.save",    "💾");
        registry.put("action.delete",  "🗑️");
        registry.put("action.search",  "🔍");
        registry.put("action.success", "✅");
        registry.put("action.error",   "❌");
        registry.put("action.warning", "⚠️");
    }

    /**
     * Pull any icon by its system key.
     */
    public String get(String key) {
        return registry.getOrDefault(key, "📦");
    }

    /**
     * Helper to get Type icons specifically for Credentials
     */
    public String forType(String typeName) {
        return get("type." + typeName.toLowerCase());
    }
}
