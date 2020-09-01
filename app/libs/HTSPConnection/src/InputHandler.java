/*
    HTSP Message Library
    Author: Waldo Theron
    Version: 0.1
 */

import java.util.Scanner;

public class InputHandler implements Runnable
{
    private Scanner scanner = new Scanner(System.in);
    private HTSPMessageHandler messageHandler;

    public InputHandler(HTSPMessageHandler messageHandler)
    {
        this.messageHandler = messageHandler;
    }

    @Override
    public void run() {
        String line = "";
        while(!line.equals("exit"))
        {
            while(scanner.hasNextLine()) {
                line = scanner.nextLine();


                if (line.equals("async")) {
                    long epgMaxTime = 0L;
                    boolean mQuickSync = false;
                    System.out.println("Enabling Async Metadata: maxTime: " + epgMaxTime + ", quickSync: " + mQuickSync);

                    HTSPMessage enableAsyncMetadataRequest = new HTSPMessage();

                    enableAsyncMetadataRequest.put("method", "enableAsyncMetadata");
                    enableAsyncMetadataRequest.put("epg", 1);

                    epgMaxTime = epgMaxTime + (System.currentTimeMillis() / 1000L);
                    enableAsyncMetadataRequest.put("epgMaxTime", epgMaxTime);

                    messageHandler.sendMessage(enableAsyncMetadataRequest);

                }

                if(line.equals("sub"))
                {
                    HTSPMessage subMessage = new HTSPMessage();

                    subMessage.put("method", "subscribe");
                    subMessage.put("channelId", 194137084);
                    subMessage.put("subscriptionId", 777);

                    messageHandler.sendMessage(subMessage);
                }

                if(line.equals("s"))
                {
                    HTSPMessage unSubMessage = new HTSPMessage();

                    unSubMessage.put("method", "unsubscribe");
                    unSubMessage.put("subscriptionId", 777);

                    messageHandler.sendMessage(unSubMessage);
                }
            }
        }
    }
}