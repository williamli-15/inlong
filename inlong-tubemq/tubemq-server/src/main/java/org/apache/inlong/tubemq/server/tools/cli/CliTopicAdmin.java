/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

 package org.apache.inlong.tubemq.server.tools.cli;

 import org.apache.inlong.tubemq.corebase.utils.TStringUtils;
 import org.apache.inlong.tubemq.server.common.fielddef.CliArgDef;
 import org.apache.inlong.tubemq.server.common.utils.HttpUtils;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.google.gson.JsonObject;
 import com.google.gson.reflect.TypeToken;
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.ParseException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.Reader;
 import java.util.HashMap;
 import java.util.Map;
 
 public class CliTopicAdmin extends CliAbstractBase {
 
     private static final Logger logger =
             LoggerFactory.getLogger(CliTopicAdmin.class);
 
     public CliTopicAdmin() {
         super("tubemq-ctl.sh");
         initCommandOptions();
     }
     @Override
     protected void initCommandOptions() {
         // initCommandOptions
         addCommandOption(CliArgDef.MASTERSERVER);
         addCommandOption(CliArgDef.TOPIC);
         addCommandOption(CliArgDef.TOPICCONFIGFILE);
     }
 
     // checkCreateJsonFile
     private boolean checkCreateJsonFile(String path) {
 
         return true;
     }
 
     @Override
     public boolean processParams(String[] args) throws Exception {
         CommandLine cli = parser.parse(options, args);
         if (cli == null) {
             throw new ParseException("[CliTopicAdmin]Parse command parameters error.");
         }
         if (cli.hasOption(CliArgDef.VERSION.longOpt)) {
             version();
         }
         if (cli.hasOption(CliArgDef.HELP.longOpt)) {
             help();
         }
         String masterServers = cli.getOptionValue(CliArgDef.MASTERSERVER.longOpt);
         if (TStringUtils.isBlank(masterServers)) {
             throw new Exception(CliArgDef.MASTERSERVER.longOpt + " is required!");
         }
         String topicStr = cli.getOptionValue(CliArgDef.TOPIC.longOpt);
         if (TStringUtils.isBlank(topicStr)) {
             throw new Exception(CliArgDef.PRDTOPIC.longOpt + " is required!");
         }
 
         String[] masterServerGroup = masterServers.split(",");
         if (masterServerGroup.length == 0) {
             throw new ParseException("[CliTopicAdmin]masterServer parse error.");
         }
 
         String webServiceUrl = "http://" + masterServerGroup[0] + "/webapi.htm";
         Map<String, String> params = new HashMap<>();
 
         String topicConfigPath = cli.getOptionValue(CliArgDef.TOPICCONFIGFILE.longOpt);
         System.out.println("-------------------");
         System.out.println(args[0]);
         System.out.println(!args[0].equals("query"));
         System.out.println(topicConfigPath);
         if (!args[0].equals("query")) {
             if (TStringUtils.isBlank(topicConfigPath)) {
                 help();
                 throw new ParseException("[CliTopicAdmin]missing topicConfig param.");
             }
             // check file exists
             File file = new File(topicConfigPath);
             if (!file.exists()) {
                 throw new ParseException("[CliTopicAdmin]topic config file does not exists.");
             }
             Reader reader = new FileReader(file);
             params = new Gson().fromJson(reader, new TypeToken<Map<String, String>>() {
             }.getType());
         }
 
         switch (args[0]) {
             case "query":
                 params.put("type", "op_query");
                 params.put("method", "admin_query_topic_info");
                 params.put("topicName", topicStr);
                 break;
             case "create":
                 params.put("type", "op_modify");
                 params.put("topicName", topicStr);
                 break;
             case "modify":
                 params.put("type", "op_modify");
                 params.put("topicName", topicStr);
                 break;
             case "delete":
                 params.put("type", "op_modify");
                 params.put("topicName", topicStr);
                 break;
             default:
                 throw new ParseException("[CliTopicAdmin]subcommand error.");
         }
 
         long start = System.currentTimeMillis();
 
         JsonObject resp = HttpUtils.requestWebService(webServiceUrl, params);
 
         Gson gson = new GsonBuilder().setPrettyPrinting().create();
         String prettyJson = gson.toJson(resp);
 
         System.out.println("\ncost time: "
                 + (System.currentTimeMillis() - start)
                 + "ms, result: \n\n"
                 + prettyJson
                 + "\n");
         return true;
     }
 
     public static void main(String[] args) {
         CliTopicAdmin cliTopicAdmin = new CliTopicAdmin();
         try {
             cliTopicAdmin.processParams(args);
         } catch (Throwable ex) {
             ex.printStackTrace();
             logger.error(ex.toString());
             cliTopicAdmin.help();
         }
     }
 
 }