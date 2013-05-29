package com.reqo.ironhold.web.components;

import com.reqo.ironhold.storage.MessageIndexService;
import com.reqo.ironhold.storage.MetaDataIndexService;
import com.reqo.ironhold.storage.MiscIndexService;
import com.reqo.ironhold.storage.model.user.LoginUser;
import com.reqo.ironhold.web.components.pagingcomponent.PagingComponent;
import com.reqo.ironhold.web.components.pagingcomponent.listener.impl.LazyPagingComponentListener;
import com.reqo.ironhold.web.components.pagingcomponent.utilities.FakeList;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.Reindeer;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * User: ilya
 * Date: 4/17/13
 * Time: 8:15 AM
 */
public class UserManagementWindow extends Window {
    private static final String NAME = "Name";
    private static final String USERNAME = "Username";
    private static final String MAIN_RECIPIENT = "Email";
    private static final String LAST_LOGIN = "Last Login";

    private static Logger logger = Logger.getLogger(UserManagementWindow.class);

    private final UserManagementWindow
            window;
    private final LoginUserViewer viewer;
    private final LoginUserEditor editor;

    private VerticalLayout userList;
    private LoginUserPanel currentUserPanel;

    public UserManagementWindow(LoginUser loginUser, final String indexPrefix, final MessageIndexService messageIndexService, final MiscIndexService miscIndexService, final MetaDataIndexService metaDataIndexService) throws ExecutionException, InterruptedException {
        super("User Management");
        this.window = this;
        setHeight("80%");
        setWidth("80%");
        setResizable(false);

        HorizontalLayout layout = new HorizontalLayout();
        this.setContent(layout);
        layout.setMargin(true);
        layout.setSpacing(true);

        VerticalLayout left = new VerticalLayout();

        userList = new VerticalLayout();
        userList.setSpacing(true);

        viewer = new LoginUserViewer(this, indexPrefix, messageIndexService, miscIndexService, metaDataIndexService);
        editor = new LoginUserEditor(this, indexPrefix, miscIndexService, metaDataIndexService);

        final List<LoginUser> results = new FakeList<>((int) miscIndexService.getLoginUserCount(indexPrefix));

        PagingComponent<LoginUser> pager = new PagingComponent<>(10, 10, results,
                new LazyPagingComponentListener<LoginUser>(userList) {

                    @Override
                    protected Collection<LoginUser> getItemsList(
                            int startIndex, int endIndex) throws Exception {
                        return miscIndexService.getLoginUsers(indexPrefix, startIndex, startIndex + 10);
                    }

                    @Override
                    protected Component displayItem(int index, LoginUser loginUser) throws Exception {
                        return new LoginUserPanel(window, loginUser, miscIndexService);
                    }

                });

        left.addComponent(pager);

        userList.setWidth("600px");
        left.addComponent(userList);

        Button addNew = new Button("Add New User");
        addNew.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                try {
                    setEditMode(null);
                } catch (InterruptedException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (ExecutionException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        });
        left.addComponent(addNew);
        VerticalLayout right = new VerticalLayout();
        viewer.setVisible(false);
        editor.setVisible(false);

        right.addComponent(viewer);
        right.addComponent(editor);

        layout.addComponent(left);
        layout.addComponent(right);
        this.addShortcutListener(new ShortcutListener("Esc", ShortcutAction.KeyCode.ESCAPE, null) {
            @Override
            public void handleAction(Object sender, Object target) {
                window.close();
            }
        });

    }

    public void setEditMode(LoginUser loginUser) throws InterruptedException, ExecutionException, IOException {
        viewer.setVisible(false);
        editor.setLoginUser(loginUser);
        editor.setVisible(true);
    }

    public void setViewMode(LoginUserPanel newUserPanel, LoginUser loginUser) throws Exception {
        if (loginUser == null) {
            editor.setVisible(false);
            viewer.setVisible(false);
        } else {
            editor.setVisible(false);
            viewer.setLoginUser(loginUser);
            viewer.setVisible(true);
        }
        if (newUserPanel != null) {
            if (currentUserPanel != null) {
                currentUserPanel.setStyleName(Reindeer.PANEL_LIGHT);
            }
            currentUserPanel = newUserPanel;
            currentUserPanel.setStyleName(null);
        }

    }
}
