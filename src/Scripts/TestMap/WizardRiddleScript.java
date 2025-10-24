//riddle script
package Scripts.TestMap;

import java.util.ArrayList;

import Level.Script;
import Level.ScriptState;
import ScriptActions.*;
import EnhancedMapTiles.Coin;

public class WizardRiddleScript extends Script {

    private boolean answered = false;

    @Override
    public ArrayList<ScriptAction> loadScriptActions() {
        ArrayList<ScriptAction> scriptActions = new ArrayList<>();

        scriptActions.add(new LockPlayerScriptAction());
        scriptActions.add(new NPCLockScriptAction());
        scriptActions.add(new NPCFacePlayerScriptAction());

        if (!answered) {
            // Show riddle with choices
            scriptActions.add(new TextboxScriptAction() {{
                addText("I speak without a mouth and hear without ears.");
                addText("I have nobody, but I come alive with the wind.");
                addText("What am I?", new String[] { "Echo", "Shadow" });
            }});

            // Conditional logic to check player's answer
            scriptActions.add(new ConditionalScriptAction() {{
                // Correct answer: Echo (choice index 0)
                addConditionalScriptActionGroup(new ConditionalScriptActionGroup() {{
                    addRequirement(new CustomRequirement() {
                        @Override
                        public boolean isRequirementMet() {
                            int answer = outputManager.getFlagData("TEXTBOX_OPTION_SELECTION");
                            return answer == 0;
                        }
                    });

                    addScriptAction(new TextboxScriptAction("Correct! You solved the riddle."));
                    addScriptAction(new ScriptAction() {
                        @Override
                        public ScriptState execute() {
                            answered = true;
                            // Add 2 coins to player's global coin count here
                            Coin.coinsCollected += 2;
                            System.out.println("[WizardRiddleScript] Added 2 coins to player. Total coins: " + Coin.coinsCollected);
                            return ScriptState.COMPLETED;
                        }
                    });
                }});

                // Wrong answers: Shadow or Wind (choice index 1 or 2)
                addConditionalScriptActionGroup(new ConditionalScriptActionGroup() {{
                    addRequirement(new CustomRequirement() {
                        @Override
                        public boolean isRequirementMet() {
                            int answer = outputManager.getFlagData("TEXTBOX_OPTION_SELECTION");
                            return answer == 1 || answer == 2;
                        }
                    });

                    addScriptAction(new TextboxScriptAction("That's not right. Try again."));
                }});
            }});
        } else {
            scriptActions.add(new TextboxScriptAction("Thanks for answering the riddle!"));
        }

        scriptActions.add(new NPCUnlockScriptAction());
        scriptActions.add(new UnlockPlayerScriptAction());

        return scriptActions;
    }
}