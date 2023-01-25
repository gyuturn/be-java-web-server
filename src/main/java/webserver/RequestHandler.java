package webserver;

import java.io.*;
import java.net.Socket;

import controller.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import request.HttpRequest;
import util.HttpStatus;
import util.error.erroclass.FailLoggedException;
import util.error.erroclass.NotLoggedException;

public class RequestHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;


    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        logger.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            HttpRequest httpRequest = HttpRequest.getHttpRequest(in);

            DataOutputStream clientOutPutStream = new DataOutputStream(out);

            try {
                ControllerFinder.findController(httpRequest, clientOutPutStream);
            } catch (FailLoggedException | NullPointerException e) {
                ErrorController.getErrorResponse(clientOutPutStream, HttpStatus.UN_AUTHORIZED);
                e.printStackTrace();
                logger.error("[ERROR]:{} {}", HttpStatus.UN_AUTHORIZED.getCode(), HttpStatus.UN_AUTHORIZED.getMessage());
                logger.error("로그인되지 않은 유저입니다. url:{}", httpRequest.getUrl().getUrl());
            }catch (NotLoggedException e) {
                ErrorController.getErrorResponse(clientOutPutStream, HttpStatus.FORBIDDEN);
                e.printStackTrace();
                logger.error("[ERROR]:{} {}", HttpStatus.FORBIDDEN.getCode(), HttpStatus.FORBIDDEN.getMessage());
                logger.error("로그인되지 않은 유저입니다. url:{}", httpRequest.getUrl().getUrl());
            }
            catch (NoSuchMethodException e) {
                e.printStackTrace();
                ErrorController.getErrorResponse(clientOutPutStream, HttpStatus.NOT_FOUND);
                logger.error("[ERROR]:{} {}", HttpStatus.NOT_FOUND.getCode(), HttpStatus.NOT_FOUND.getMessage());
                logger.error("FileReaderContoller에서 url에 맞는 페이지가 없습니다. url:{}", httpRequest.getUrl().getUrl());
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("[ERROR]:{} {}", HttpStatus.INTERNAL_SERVER_ERROR.getCode(), HttpStatus.INTERNAL_SERVER_ERROR.getMessage());
                logger.error("서버에서 처리못한 에러. controller:{}, url:{}", httpRequest.getUrl().getUrl());
                ErrorController.getErrorResponse(clientOutPutStream, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }


}


