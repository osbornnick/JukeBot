package com.osbornnick.jukebot;

public class Message {

        public String messageText;
        public String messageUser;
        public String messageTime;

        public Message(String messageText, String messageUser, String messageTime) {
            this.messageText = messageText;
            this.messageUser = messageUser;
            this.messageTime = messageTime;
        }

        public Message(){

        }

        public String getMessageText() {
            return messageText;
        }

        public void setMessageText(String messageText) {
            this.messageText = messageText;
        }

        public String getMessageUser() {
            return messageUser;
        }

        public void setMessageUser(String messageUser) {
            this.messageUser = messageUser;
        }

        public String getMessageTime() {
            return messageTime;
        }


}
