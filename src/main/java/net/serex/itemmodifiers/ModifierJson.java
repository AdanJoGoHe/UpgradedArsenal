package net.serex.upgradedarsenal;

import java.util.List;

public class ModifierJson {
    public String id;
    public String name;
    public String type;
    public String rarity;
    public List<AttributeJson> attributes;

    public static class AttributeJson {
        public String attribute;
        public double value;
        public String operation;
    }
}

