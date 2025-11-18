//riddle script
package Scripts.TestMap;

import java.util.ArrayList;

import Level.Script;
import Level.ScriptState;
import ScriptActions.*;
import EnhancedMapTiles.Coin;
import EnhancedMapTiles.DoorKey;
import Level.Player;
import Level.NPC;

public class KeyWizardRiddleScript2 extends Script {

    private boolean answered = false;
    //reference to sada and wizard
    protected Player player;
    private NPC wizard;

    public KeyWizardRiddleScript2(Player player, NPC wizard) {
        this.player = player;
        this.wizard = wizard;
    }
    
    @Override
    public ArrayList<ScriptAction> loadScriptActions() {
        ArrayList<ScriptAction> scriptActions = new ArrayList<>();

        scriptActions.add(new LockPlayerScriptAction());
        scriptActions.add(new NPCLockScriptAction());
        scriptActions.add(new NPCFacePlayerScriptAction());

        if (!answered) {
            // Show riddle with choices
            scriptActions.add(new TextboxScriptAction() {{
                addText("What always runs but never walks.");
                addText("Often murmurs, never talks.");
                addText("Has a bed but never sleeps.");
                addText("An open mouth that never eats?", new String[] { "Waterfall", "River" });
            }});

            // Conditional logic to check player's answer
            scriptActions.add(new ConditionalScriptAction() {{
                // Correct answer: Echo (choice index 0)
                addConditionalScriptActionGroup(new ConditionalScriptActionGroup() {{
                    addRequirement(new CustomRequirement() {
                        @Override
                        public boolean isRequirementMet() {
                            int answer = outputManager.getFlagData("TEXTBOX_OPTION_SELECTION");
                            return answer == 1;
                        }
                    });

                    addScriptAction(new TextboxScriptAction("Correct! You solved the riddle."));
                    addScriptAction(new ScriptAction() {
                        @Override
                        public ScriptState execute() {
                            answered = true;
                            // Add 2 coins to player's global coin count here
                            DoorKey.keysCollected ++;
                            System.out.println("[WizardRiddleScript] Added 2 coins to player. Total coins: " + Coin.coinsCollected);
                            //remove wizard when player correctly answers
                            if (wizard != null) {
                                wizard.removeNPC();
                                System.out.println("[WizardRiddleScript] Wizard removed from the map.");
                            }

                            return ScriptState.COMPLETED;
                        }
                    });
                }});

                //wrong answer take damage
                addConditionalScriptActionGroup(new ConditionalScriptActionGroup() {{
                    addRequirement(new CustomRequirement() {
                        @Override
                        public boolean isRequirementMet() {
                            int answer = outputManager.getFlagData("TEXTBOX_OPTION_SELECTION");
                            return answer == 1 || answer == 2;
                        }
                    });

                    addScriptAction(new TextboxScriptAction("How dare you?"));
                    addScriptAction(new ScriptAction() {
                        @Override
                        public ScriptState execute() {
                            answered = false;
                            // Take away a heart of health
                            player.takeDamage(2);
                            System.out.println("[WizardRiddleScript] The wizard got mad and poisoned you!  You have lost 1 heart!");
                            return ScriptState.COMPLETED;
                        }
                    });
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