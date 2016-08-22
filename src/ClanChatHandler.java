import org.osbot.rs07.api.ui.RS2Widget;
import org.osbot.rs07.api.ui.Tab;
import org.osbot.rs07.script.Script;

import java.util.Random;

public class ClanChatHandler {

    private Script script;

    public ClanChatHandler(Script script){
        this.script = script;
    }

    public boolean notInClanChat(){
        RS2Widget notInChat = script.getWidgets().getWidgetContainingText("Not in chat");
        return (notInChat != null && notInChat.isVisible());
    }

    public void openClanChatTab(){
        script.getTabs().open(Tab.CLANCHAT);
    }

    public boolean isClanChatTabOpen(){
        return Tab.CLANCHAT.isOpen(script.getBot());
    }

    public boolean isInClanChat(){
        RS2Widget talkingIn = script.getWidgets().getWidgetContainingText("Talking in:");
        return (talkingIn != null && talkingIn.isVisible() && !talkingIn.getMessage().contains("Not in chat"));
    }

    public void joinClanChat(String name){
        RS2Widget enterName = script.getWidgets().get(162, 32);
        RS2Widget joinChat = script.getWidgets().getWidgetContainingText("Join Chat");

        if (joinChat != null && joinChat.isVisible()) {
            clickOnWidget(joinChat);
        }
        if (enterName != null && enterName.isVisible()){
            script.getKeyboard().typeString(name);
        }
    }

    public void leaveClanChat(){
        RS2Widget leaveWidget = script.getWidgets().getWidgetContainingText("Leave Chat");
        if (leaveWidget != null && leaveWidget.isVisible()) clickOnWidget(leaveWidget);
    }

    public boolean isInClan(String name){
        if (script.getWidgets().get(589,0) != null) {//script.getWidgets().getWidgetContainingText("Talking in:") != null) {
            RS2Widget talkingIn = script.getWidgets().get(589,0);
            String s = talkingIn.getMessage().replaceAll("^.*(?=(Iron))", "");
            //script.log(s.toLowerCase() + " vs " + name.toLowerCase());
            //script.log(s.toLowerCase().contains(name.toLowerCase()));
            return s.toLowerCase().contains(name.toLowerCase());
            }
        else {
            return false;
        }
    }

    public boolean isOwner(String name){
        RS2Widget owner = script.getWidgets().getWidgetContainingText("Owner:");
        return owner.getMessage().contains(name);
    }

    public void talkInClanChat(String output){
        script.getKeyboard().typeString(String.format("/%s", output));
        sleep(1500, 2000);
    }

    private void clickOnWidget(RS2Widget widget){
        widget.hover();
        script.getMouse().click(false);
        sleep(1500, 2000);
    }

    private void sleep(int min, int max){

        try{
            script.sleep(new Random().nextInt(max-min) + min);
        } catch(Exception e){
            script.log(e);
        }
    }
}