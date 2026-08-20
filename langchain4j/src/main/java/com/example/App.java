package com.example;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        ChatModel openAiChatModel = OpenAiChatModel.builder()
                                                .apiKey("sk-ws-H.EYMDPRL.XhIH.MEQCIDYUCccAnq5u6Df7hi23dNdb5J5UdJXQP3ixkfNWnPO8AiBq0tfSSlf_YRYDojBuczcFb9djr4X8jn3XSZls1EK0Ag")
                                                .baseUrl("https://ws-unb6ehiw77dt9w5k.cn-beijing.maas.aliyuncs.com/compatible-mode/v1")
                                                .modelName("qwen3.8-max")
                                                .build(); ;
        String res = openAiChatModel.chat("你好,你是谁");
        System.out.println(res);


        System.out.println( "Hello World!" );
    }
}
