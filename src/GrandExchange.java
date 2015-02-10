import org.powerbot.script.rt6.ClientContext;
import org.powerbot.script.Area;
import org.powerbot.script.Condition;
import org.powerbot.script.Random;
import org.powerbot.script.Tile;
import org.powerbot.script.rt6.Component;
import org.powerbot.script.rt6.Item;
import org.powerbot.script.rt6.Npc;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: Beta_Guru
 * Time: 16:48
 */
public class GrandExchange extends ClientContext {
    public GrandExchange(ClientContext ctx) {
        super(ctx);
    }
    /**
     * Grand Exchange in-game interaction API.
     *
     * @authors Javaskill, Aion, Boolean, Debauchery, kyleshay, Beta_Guru
     */

    private final int GRAND_EXCHANGE = 105;
    private final int BUY_SEARCH_BOX = 389;
    private final int SEARCH_BOX_ITEM_LIST = 4;
    private final int TRANSACTION_INFO_ID = 198;   //105 -> 198 STRING[]
    private Pattern p = Pattern.compile("(.?)<col=cc9900>(.*)</col><br>for a total price of <col=cc9900>(.*)</col>");
    //Pattern p1 = Pattern.compile("(>([0-9]{1,3},?)+)");

    //
    private final int[] GRAND_EXCHANGE_BUY_BUTTON = {31, 47, 63, 82, 101, 120};
    private final int[] GRAND_EXCHANGE_SELL_BUTTON = {32, 48, 64, 83, 102, 121};
    private final int[] GRAND_EXCHANGE_OFFER_BOXES = {19, 35, 51, 67, 83, 99};

    //For Collection
    @SuppressWarnings("unused")
	private final int GRAND_EXCHANGE_COLLECT_BOX_ONE = 209;
    @SuppressWarnings("unused")
	private final int GRAND_EXCHANGE_COLLECT_BOX_TWO = 211;

    //For NPCs
    private final int[] GRAND_EXCHANGE_CLERK = {6528, 6529,1419, 2240, 2241, 2593};

    /**
     * Gets the Grand Exchange interface
     * @return
     */
    public Component getComponent() {
        return widgets.component(GRAND_EXCHANGE,0);
    }

    /**
     *
     * @param slot
     * @return
     */
    public boolean isFree(int slot){
        final int slotComponent = GRAND_EXCHANGE_OFFER_BOXES[slot - 1];
        return isOpen() && widgets.component(GRAND_EXCHANGE,slotComponent).component(10).text().contains("Empty");
    }

    public int getFree(boolean members) {
    int maxSlots = 6;
        if(!members)
            maxSlots = 2;

        for(int i = 1; i <= maxSlots; i++){
            if(isFree(i))
                return i;
        }
        return -1;
    }

    public int freeSlot(){
        for(int i = 1; i <= 6; i++){
            if(checkSlotIsEmpty(i)){
                return i;
            }
        }
        return 0;
    }

    @SuppressWarnings("null")
	public int[] freeSlots() throws AssertionError{
        List<Integer> intList = null;

        try {
            for (int i = 0; i < 6; i++){
                if(checkSlotIsEmpty(i)){
                    assert false;
                    intList.add(i);
                }
            }
            assert false;
            if(intList.size() > 0){
                return convertIntergers(intList);
            }
        } catch (Error ignored){}
        return new int[]{0};
    }

    public void openFreeBuySlot(){
        int slot = freeSlot();
        openBuy(slot);
    }

    public void openFreeSellSlot(){
        int slot = freeSlot();
        openSell(slot);
    }

    public boolean atGE(){
    	//TODO Initialise properly
        Tile t1 = new Tile(3156,3477);
        Tile t2 = new Tile(3176,3501);
        Area GE_AREA = new Area(t1,t2);
        return GE_AREA.contains(players.local().tile());
    }

    public boolean closeGe(){
        if(isOpen()){
            Component exit = widgets.component(GRAND_EXCHANGE,14);
            exit.click();
            Condition.sleep(Random.getDelay());
        }
        return true;
    }

    public boolean chooseBuyingItem(String item){
        int infid = -1;
        Component c = widgets.component(GRAND_EXCHANGE,142);
        Component c1 = widgets.component(GRAND_EXCHANGE,189);
        Component[] list = widgets.component(BUY_SEARCH_BOX,SEARCH_BOX_ITEM_LIST).components();
        Component itemResult;

        if(!c.text().equalsIgnoreCase(item)){
            c1.click();
            Condition.sleep(Random.getDelay());
            input.send(item);
            Condition.sleep(Random.getDelay());
            for(int i = 1; i < list.length; i++){
                if(list[i].text().equalsIgnoreCase(item)){
                    infid = i;
                    break;
                }
            }
            if(infid != -1){
                itemResult = widgets.component(BUY_SEARCH_BOX,SEARCH_BOX_ITEM_LIST).component(infid);
                if(itemResult.visible() || itemResult.inViewport()){
                    itemResult.click();
                    Condition.sleep(Random.getDelay());
                    return true;
                } else {
                    //The id exists, however it is out of reach.
                    //TODO Scroll to item in search-box
                    return false;
                }
            }
        }
        return false;
    }

    public boolean searchItem(String item) throws InterruptedException{
        return chooseBuyingItem(item);
    }

    public int findItem(final String name){
        for(int i = 1; i<=6; i++){
            if(isOpen()){
                if(checkSlotIsEmpty(i)){
                    final int slotcomp = GRAND_EXCHANGE_OFFER_BOXES[i];
                    final String s = widgets.component(slotcomp,18).text();
                    if(s.equalsIgnoreCase(name)){
                        return i;
                    }
                }
            }
        }
        return -1;
    }
    public  boolean open() throws InterruptedException{
        Npc geClerk = npcs.select().id(GRAND_EXCHANGE_CLERK).poll();

        if(isOpen()) {
            return true;
        } else {
            if (atGE()) {
                if (movement.reachable(players.local().tile(), geClerk.tile())) {
                    movement.step(geClerk);
                    geClerk.interact(true, "Exchange"); //TODO check this out
                    Condition.wait(new Callable<Boolean>() {
                        @Override
                        public Boolean call() throws Exception {
                            return isOpen();
                        }
                    }, 150, 50);
                } else {
                    System.out.print("NPC unreachable.");
                    return false;
                }
            }
        }
        return false;
    }

    public boolean isSellOpen(){
        Component c = widgets.component(GRAND_EXCHANGE, 134);
        Component BUY_BAG = widgets.component(GRAND_EXCHANGE, 128);

        if (c.valid() && c.text().equalsIgnoreCase("Sell Offer")) {
            if (BUY_BAG.valid() && BUY_BAG.visible() || BUY_BAG.inViewport()) {
                return true;
            }
        }
        return false;
    }

    public boolean isBuyOpen() {
        Component c = widgets.component(GRAND_EXCHANGE, 134);
        Component BUY_BAG = widgets.component(GRAND_EXCHANGE, 128);

        if (c.valid() && c.text().equalsIgnoreCase("Buy Offer")) {
            if (BUY_BAG.valid() && BUY_BAG.visible() || BUY_BAG.inViewport()) {
                return true;
            }
        }
        return false;
    }

    public boolean checkSlotIsEmpty(final int slot){
        final int slotComponent = GRAND_EXCHANGE_OFFER_BOXES[slot];
        Component middle = widgets.component(GRAND_EXCHANGE,slotComponent).component(10);
        for(Component child : middle.components()){
            if(!child.text().isEmpty()){
                if (child.text().contains("Empty")){
                    return true;
                }
            }
        }
        return false;
    }

    public boolean validSlotNumber(int slot){
        return (slot > 0);
    }

    public boolean checkCompleted(final int slot){
        if(validSlotNumber(slot)) {
            if (!checkSlotIsEmpty(slot)) {
                final int slotComponent = GRAND_EXCHANGE_OFFER_BOXES[slot];
                Component slot_title = widgets.component(GRAND_EXCHANGE, slotComponent);
                for (Component child : slot_title.components()) {
                    if (!child.text().isEmpty()) {
                        if (!child.text().equalsIgnoreCase("Abort offer")) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public void closeGE(){
    	@SuppressWarnings("unused")
		Component exit;
        if(isOpen()){
            exit = widgets.component(GRAND_EXCHANGE,14);
        }
    }

    public int getTransactionPrice(){
        Component transId = widgets.component(GRAND_EXCHANGE,TRANSACTION_INFO_ID);
        if(transId!=null) {
            return Integer.parseInt(p.matcher(transId.text()).group(3));
        }
        return 0;
    }

    public int getTransactionQuantity(){
        Component transIds = widgets.component(GRAND_EXCHANGE, TRANSACTION_INFO_ID);
        if(transIds!=null){
            return Integer.parseInt(p.matcher(transIds.text()).group(3));
        }
        return 0;
    }

    public boolean isOpen(){
        return getComponent().visible()||getComponent()!=null;
    }

    public boolean isTransactionOpen(){
        Component tw = widgets.component(GRAND_EXCHANGE,TRANSACTION_INFO_ID);
        return tw!=null;
    }

    public void setQuantity(int quantity){
        Component qty = widgets.component(GRAND_EXCHANGE,168);
        if(qty != null && isOpen()){
            qty.click();
            Condition.sleep(Random.getDelay());
            input.send(quantity + "{VK_ENTER}");
            Condition.sleep(Random.getDelay());
        }
    }

    public boolean isSellItemChosen(){
        Component chosen = widgets.component(GRAND_EXCHANGE,142);
        return chosen!=null && isOpen() && isSellOpen() && !chosen.text().contains("hoose an item");
    }

    public boolean isBuyItemChosen(){
        Component chosen = widgets.component(GRAND_EXCHANGE,142);
        return isOpen() && isBuyOpen() && !chosen.text().contains("hoose an item");
    }

    public boolean clickConfirm(){
        Component c1 = widgets.component(GRAND_EXCHANGE,186);
        if(c1.valid() && c1.visible()){
            c1.click();
            Condition.sleep(Random.getDelay());
        }
        return false;
    }

    public boolean clickBackButton(){
        Component c1 = widgets.component(GRAND_EXCHANGE,128);
        if(c1.valid() && c1.visible()){
            c1.click();
            Condition.sleep(Random.getDelay());
            return true;
        }
        Condition.sleep(Random.getDelay());
        return false;
    }

    public boolean sellAll(){
        Component c = widgets.component(GRAND_EXCHANGE,166);
        if(c.valid() && c.visible()){
            c.click();
            return true;
        }
        return false;
    }

    public boolean sellX(String text){
        Component c = widgets.component(GRAND_EXCHANGE,166);
        if(c.valid() && c.visible()){
            c.click();
            Condition.sleep(Random.getDelay());
            input.send(text + "{VK_ENTER}");
            Condition.sleep(Random.getDelay());
            return true;
        }
        return false;
    }

    public boolean fivePercentDown(){
        Component c = widgets.component(GRAND_EXCHANGE,181);
        if(c.visible() && c.valid()){
            c.click();
            Condition.sleep(Random.getDelay());
            return true;
        }
        return false;
    }

    public boolean fivePercentUp(){
        Component c = widgets.component(GRAND_EXCHANGE,179);
        if(c.visible() && c.valid()){
            c.click();
            Condition.sleep(Random.getDelay());
            return true;
        }
        return false;
    }

    public boolean sellUp(Item item, int Quantity, int upButtonNumber, Boolean Members) throws InterruptedException {
        boolean success = false;

        if (!item.valid())
            return false;

        if(isSellOpen()){
            while ((!isSellItemChosen())){
                if(item.valid()){
                    item.click();
                    Condition.sleep(Random.getDelay());
                } else {
                    return false;
                }
            }
            if(isSellItemChosen()){
                if(upButtonNumber > 0){
                    for(int i = 0; i < upButtonNumber; i++) {
                        fivePercentUp();
                    }
                }
                if(Quantity == 0){
                    sellAll();
                    Condition.sleep(Random.getDelay());
                }
                if(Quantity >= 1){
                    setQuantity(Quantity);
                    Condition.sleep(Random.getDelay());
                }
                success = clickConfirm();
            }
        }

        return success;
    }

    private boolean openSlot(final int slot, boolean buy){
        if(!isOpen()){
            return false;
        }
        int c = buy ? GRAND_EXCHANGE_BUY_BUTTON[slot - 1] : GRAND_EXCHANGE_SELL_BUTTON[slot - 1];
        return buy ?
                widgets.component(GRAND_EXCHANGE,c).interact(true,"Make Buy Offer") :
                widgets.component(GRAND_EXCHANGE,c).interact(true,"Make Sell Offer");
    }

    public boolean openBuy(final int slot){
        return openSlot(slot,true);
    }

    public boolean openSell(final int slot){
        return openSlot(slot,false);
    }

    /**
     * Modified from: https://github.com/powerbot/RSBot-API/blob/master/util/net/GeItem.java
     *
     * @param number number to be parsed
     * @return Gets the new number and adds suffix of "b", "m" or "k"
     */
    public static String parseNumber(String number) {

        final int multiplier;
        String suffix;
        switch (number.length()) {
            case 9:
                multiplier = 100000000; // 1 billion (x10^9)
                suffix = "B";
                break;
            case 6: case 7: case 8:
                multiplier = 1000000;
                suffix = "M";          // 1 million (x10^6)
                break;
            case 3: case 4: case 5:
                multiplier = 1000;
                suffix = "k";          // 1 thousand (x10^3)
                break;
            default:
                multiplier = 1;       //  One (x10^1)
                suffix = "gp";
        }
        int inital = Integer.parseInt(number);
        int parsed = inital % multiplier;

        return String.valueOf(parsed + suffix);

    }

    public int[] convertIntergers(List<Integer> integers){
        int[] ret = new int[integers.size()];
        Iterator<Integer> iterator = integers.iterator();
        for(int i = 0; i < ret.length; i++) {
        ret[i] = iterator.next();
        }
        return ret;
    }




}
