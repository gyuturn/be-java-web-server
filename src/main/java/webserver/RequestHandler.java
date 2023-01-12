package webserver;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;

import controller.Controller;
import controller.FileController;
import controller.UserController;
import db.Database;
import db.UserDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reader.RequestGetReader;
import reader.RequestReader;
import request.HttpRequest;
import response.HttpResponse;
import service.Service;
import service.UserService;
import util.FileType;
import util.UrlType;

public class RequestHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    private Controller controller;



    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        logger.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            HttpRequest httpRequest = HttpRequest.getHttpRequest(in);

            DataOutputStream clientOutPutStream = new DataOutputStream(out);

            controller = Controller.FactoryController(httpRequest);

            HttpResponse httpResponse;
            UrlType urlType = UrlType.getUrlType(httpRequest.getUrl().getUrl());
            if (controller instanceof FileController) {
                if (urlType.equals(UrlType.TEMPLATES_FILE)) {
                    httpResponse=((FileController) controller).TemplateController(clientOutPutStream, httpRequest);
                } else if (urlType.equals(UrlType.STATIC_FILE)) {
                    httpResponse=((FileController) controller).StaticController(clientOutPutStream, httpRequest);
                }
            } else if (controller instanceof UserController) {
                httpResponse = ((UserController) controller).UserQueryString(clientOutPutStream, httpRequest);
            }



        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }
}

