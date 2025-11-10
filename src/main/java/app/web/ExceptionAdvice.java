package app.web;

import app.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MissingRequestValueException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.resource.NoResourceFoundException;



@ControllerAdvice
public class ExceptionAdvice {


    @ExceptionHandler(UsernameAlreadyExistException.class)
    public String handleUsernameAlreadyExist(HttpServletRequest request, RedirectAttributes redirectAttributes, UsernameAlreadyExistException exception) {

        String message = exception.getMessage();

        redirectAttributes.addFlashAttribute("usernameAlreadyExistMessage", message);
        return "redirect:/register";
    }



    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(
            {AccessDeniedException.class,
                    UnauthorizedAccessException.class })
    public ModelAndView handleAccessDenied(Exception exception) {
        ModelAndView modelAndView = new ModelAndView("access-denied");
        modelAndView.addObject("errorMessage", exception.getMessage());
        return modelAndView;
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({
            CategoryNotFoundException.class,
            NoResourceFoundException.class,
            MethodArgumentTypeMismatchException.class,
            MissingRequestValueException.class,
            CommentNotFoundException.class,
            UserNotFoundException.class,
            RecipeNotFoundException.class})

    public ModelAndView handleNotFoundExceptions(Exception exception) {

        return new ModelAndView("not-found");
    }


    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ModelAndView handleAnyException(Exception exception) {

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("internal-server-error");
        modelAndView.addObject("errorMessage", exception.getClass().getSimpleName());

        return modelAndView;
    }


    @ExceptionHandler(LastAdminException.class)
    public String handleLastAdminException(LastAdminException exception,
                                           RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        return "redirect:/admin";
    }




}