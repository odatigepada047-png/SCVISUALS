package moscow.rockstar.systems.modules.modules.other;

import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.systems.setting.settings.StringSetting;
import moscow.rockstar.utility.game.EntityUtility;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ModuleInfo(name = "Hider Utils", category = ModuleCategory.OTHER, enabledByDefault = true, desc = "modules.descriptions.hider_utils")
public class HiderUtils extends BaseModule {
    
    private final BooleanSetting hidePasswords = new BooleanSetting(this, "modules.settings.hider_utils.hide_passwords").enabled(true);
    private final BooleanSetting hideAnarchy = new BooleanSetting(this, "modules.settings.hider_utils.hide_anarchy").enabled(true);
    private final BooleanSetting hidePrivileges = new BooleanSetting(this, "modules.settings.hider_utils.hide_privileges").enabled(true);
    private final BooleanSetting hideNick = new BooleanSetting(this, "modules.settings.hider_utils.hide_nick").enabled(true);
    private final StringSetting fakeName = new StringSetting(this, "modules.settings.hider_utils.fake_name").text("Player");

    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
        "^(?:§.)*([/\\.](?:l|login|reg|register|д|куп))(\\s+)(.*)$", 
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern ANARCHY_PATTERN = Pattern.compile(
        "(?i)(анархия|anarchy|гриф|grief|mst)(\\s*[-_\\s]?\\s*)(\\d+)"
    );

    private static final Pattern PRIVILEGE_PATTERN = Pattern.compile(
        "(?i)(ранг|ранк|rank)(\\s*:\\s*)(.+)"
    );

    public String patchName(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        // 1. Hide Passwords
        if (this.hidePasswords.isEnabled()) {
            text = maskPasswords(text);
        }

        // 2. Hide Anarchy
        if (this.hideAnarchy.isEnabled()) {
            text = maskAnarchy(text);
        }

        // 3. Hide Privileges
        if (this.hidePrivileges.isEnabled()) {
            text = maskPrivileges(text);
        }

        // 4. Nickname Protect
        if (this.hideNick.isEnabled()) {
            String clientUsername = mc.getUser().getName();
            String replacement = this.fakeName.getText();
            if (replacement == null || replacement.isEmpty()) {
                replacement = "Player";
            }
            if (EntityUtility.isInGame() && mc.player != null) {
                var displayName = mc.player.getDisplayName();
                if (displayName != null) {
                    text = text.replace(displayName.getString(), replacement);
                }
            }
            text = text.replace(clientUsername, replacement);
        }

        return text;
    }

    private String maskPasswords(String text) {
        Matcher matcher = PASSWORD_PATTERN.matcher(text);
        if (matcher.matches()) {
            String prefix = text.substring(0, matcher.start(1));
            String cmd = matcher.group(1);
            String space = matcher.group(2);
            String args = matcher.group(3);
            
            StringBuilder maskedArgs = new StringBuilder();
            for (int i = 0; i < args.length(); i++) {
                char c = args.charAt(i);
                if (Character.isWhitespace(c)) {
                    maskedArgs.append(c);
                } else {
                    maskedArgs.append('*');
                }
            }
            return prefix + cmd + space + maskedArgs.toString();
        }
        return text;
    }

    private String maskAnarchy(String text) {
        Matcher matcher = ANARCHY_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.replaceAll("$1$2***");
        }
        return text;
    }

    private String maskPrivileges(String text) {
        Matcher matcher = PRIVILEGE_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.replaceAll("$1$2User");
        }
        return text;
    }

    public BooleanSetting getHidePasswords() {
        return this.hidePasswords;
    }

    public BooleanSetting getHideAnarchy() {
        return this.hideAnarchy;
    }

    public BooleanSetting getHidePrivileges() {
        return this.hidePrivileges;
    }

    public BooleanSetting getHideNick() {
        return this.hideNick;
    }

    public StringSetting getFakeName() {
        return this.fakeName;
    }

    public static net.minecraft.network.chat.Component patchComponent(net.minecraft.network.chat.Component component) {
        if (component == null) return null;
        HiderUtils hider = moscow.rockstar.Rockstar.getInstance().getModuleManager().getModule(HiderUtils.class);
        if (hider == null || !hider.isEnabled()) {
            return component;
        }

        String plain = component.getString();
        boolean hasPriv = hider.getHidePrivileges().isEnabled() && (plain.toLowerCase().contains("ранг") || plain.toLowerCase().contains("ранк") || plain.toLowerCase().contains("rank"));
        boolean hasAnarchy = hider.getHideAnarchy().isEnabled() && (plain.toLowerCase().contains("анархия") || plain.toLowerCase().contains("anarchy") || plain.toLowerCase().contains("гриф") || plain.toLowerCase().contains("grief") || plain.toLowerCase().contains("mst"));
        boolean hasFakeName = hider.getHideNick().isEnabled() && moscow.rockstar.utility.game.EntityUtility.isInGame() && hider.mc.player != null && plain.contains(hider.mc.getUser().getName());

        if (!hasPriv && !hasAnarchy && !hasFakeName) {
            return component;
        }

        ComponentReplacer replacer = new ComponentReplacer(hider);
        return replacer.replace(component);
    }

    private static class ComponentReplacer {
        private final HiderUtils hider;
        private boolean insidePrivilege = false;
        private boolean insideAnarchy = false;

        public ComponentReplacer(HiderUtils hider) {
            this.hider = hider;
        }

        public net.minecraft.network.chat.Component replace(net.minecraft.network.chat.Component component) {
            if (component == null) return null;

            net.minecraft.network.chat.ComponentContents contents = component.getContents();
            net.minecraft.network.chat.ComponentContents newContents = contents;

            if (contents instanceof net.minecraft.network.chat.contents.PlainTextContents plainText) {
                String text = plainText.text();
                String newText = text;

                if (hider.getHidePrivileges().isEnabled()) {
                    if (insidePrivilege) {
                        newText = "User";
                        insidePrivilege = false;
                    } else {
                        java.util.regex.Matcher m = PRIVILEGE_PATTERN.matcher(text);
                        if (m.find()) {
                            newText = m.replaceAll("$1$2User");
                        } else if (text.toLowerCase().contains("ранг:") || text.toLowerCase().contains("ранк:") || text.toLowerCase().contains("rank:")) {
                            insidePrivilege = true;
                        }
                    }
                }

                if (hider.getHideAnarchy().isEnabled()) {
                    if (insideAnarchy) {
                        newText = text.replaceAll("\\d+", "***");
                        insideAnarchy = false;
                    } else {
                        java.util.regex.Matcher m = ANARCHY_PATTERN.matcher(text);
                        if (m.find()) {
                            newText = m.replaceAll("$1$2***");
                        } else if (text.toLowerCase().contains("анархия") || text.toLowerCase().contains("anarchy") || text.toLowerCase().contains("гриф") || text.toLowerCase().contains("grief") || text.toLowerCase().contains("mst")) {
                            insideAnarchy = true;
                        }
                    }
                }

                if (hider.getHideNick().isEnabled()) {
                    String clientUsername = hider.mc.getUser().getName();
                    String replacement = hider.getFakeName().getText();
                    if (replacement == null || replacement.isEmpty()) {
                        replacement = "Player";
                    }
                    newText = newText.replace(clientUsername, replacement);
                    if (moscow.rockstar.utility.game.EntityUtility.isInGame() && hider.mc.player != null) {
                        var displayName = hider.mc.player.getDisplayName();
                        if (displayName != null) {
                            newText = newText.replace(displayName.getString(), replacement);
                        }
                    }
                }

                if (!newText.equals(text)) {
                    newContents = net.minecraft.network.chat.contents.PlainTextContents.create(newText);
                }
            }

            java.util.List<net.minecraft.network.chat.Component> siblings = component.getSiblings();
            java.util.List<net.minecraft.network.chat.Component> newSiblings = new java.util.ArrayList<>();
            boolean siblingsChanged = false;
            for (net.minecraft.network.chat.Component sibling : siblings) {
                net.minecraft.network.chat.Component newSibling = replace(sibling);
                if (newSibling != sibling) {
                    siblingsChanged = true;
                }
                newSiblings.add(newSibling);
            }

            if (newContents != contents || siblingsChanged) {
                net.minecraft.network.chat.MutableComponent newComp = net.minecraft.network.chat.MutableComponent.create(newContents).withStyle(component.getStyle());
                for (net.minecraft.network.chat.Component s : newSiblings) {
                    newComp.append(s);
                }
                return newComp;
            }

            return component;
        }
    }
}

