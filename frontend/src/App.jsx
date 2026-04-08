import { useEffect, useMemo, useRef, useState } from 'react';
import { Link, Navigate, Route, Routes, useNavigate } from 'react-router-dom';
import {
  apiFetch,
  clearSession,
  getCurrentUser,
  getToken,
  login,
  register,
  saveSession
} from './api';
import introGuide from './assets/intro-guide.jpg';
import githubIcon from './assets/github.jpg';
import instagramIcon from './assets/instagram.jpg';
import googleTasksLogo from './assets/google-tasks.jpg';

const STATUSES = ['TODO', 'IN_PROGRESS', 'DONE'];

const STATUS_LABEL = {
  TODO: 'To Do',
  IN_PROGRESS: 'In Progress',
  DONE: 'Done'
};

function ProtectedRoute({ children }) {
  if (!getToken()) {
    return <Navigate to="/signin" replace />;
  }

  return children;
}

function LandingPage() {
  const footerRef = useRef(null);
  const [footerVisible, setFooterVisible] = useState(false);

  useEffect(() => {
    const observer = new IntersectionObserver(
      ([entry]) => setFooterVisible(entry.isIntersecting),
      { threshold: 0.2 }
    );

    if (footerRef.current) {
      observer.observe(footerRef.current);
    }

    return () => observer.disconnect();
  }, []);

  return (
    <div className="landing-page">
      <header className="landing-header">
        <div className="landing-header-inner">
          <h1>Task Manager</h1>
          <div className="header-auth-actions">
            <Link className="btn btn-soft" to="/signin">Sign In</Link>
            <Link className="btn btn-ghost" to="/signup">Sign Up</Link>
          </div>
        </div>
      </header>

      <main className="landing-main">
        <img className="intro-image" src={introGuide} alt="Great guides" />
        <div className="intro-actions">
          <Link className="btn btn-primary" to="/signup">Get Started</Link>
        </div>
      </main>

      <section
        ref={footerRef}
        className={footerVisible ? 'landing-footer reveal' : 'landing-footer'}
      >
        <div className="footer-brand-area">
          <h2>Task Manager</h2>
          <p>Build your day with clean planning and clear priorities.</p>
          <small>Development year: 2026</small>
        </div>

        <div className="footer-col">
          <h4>Quick links</h4>
          <span>Landing</span>
          <span>Sign in</span>
          <span>Sign up</span>
        </div>

        <div className="footer-col">
          <h4>Contacts</h4>
          <div className="social-links">
            <a href="https://github.com/GyanPosling" target="_blank" rel="noreferrer">
              <img src={githubIcon} alt="GitHub" />
            </a>
            <a href="https://www.instagram.com/0_antn_0?igsh=NDd4NzBmajNkdDJr" target="_blank" rel="noreferrer">
              <img src={instagramIcon} alt="Instagram" />
            </a>
          </div>
        </div>
      </section>
    </div>
  );
}

function AuthPage({ mode }) {
  const navigate = useNavigate();
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  const isSignIn = mode === 'signin';

  async function onSubmit(event) {
    event.preventDefault();
    setBusy(true);
    setError('');

    try {
      if (isSignIn) {
        const data = await login(username, password);
        saveSession(data);
        navigate('/app');
      } else {
        await register(username, email, password);
        navigate('/signin', { replace: true });
      }
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="auth-page">
      <form className="auth-box" onSubmit={onSubmit}>
        <img className="auth-logo" src={googleTasksLogo} alt="Task Manager logo" />
        <h2>{isSignIn ? 'Sign In' : 'Sign Up'}</h2>

        <label className="field">
          <span>Username</span>
          <input value={username} onChange={(e) => setUsername(e.target.value)} required maxLength={50} />
        </label>

        {!isSignIn && (
          <label className="field">
            <span>Email</span>
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              maxLength={255}
            />
          </label>
        )}

        <label className="field">
          <span>Password</span>
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            minLength={8}
            maxLength={128}
          />
        </label>

        {error && <div className="error-box">{error}</div>}

        <button className="btn btn-primary full" disabled={busy} type="submit">
          {busy ? 'Please wait...' : isSignIn ? 'Sign In' : 'Create account'}
        </button>

        <div className="auth-links">
          <Link to={isSignIn ? '/signup' : '/signin'}>
            {isSignIn ? 'Need account?' : 'Already have account?'}
          </Link>
          <Link to="/">Back to intro</Link>
        </div>
      </form>
    </div>
  );
}

function DashboardPage() {
  const navigate = useNavigate();
  const [user] = useState(getCurrentUser());
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const [users, setUsers] = useState([]);
  const [projects, setProjects] = useState([]);
  const [tasks, setTasks] = useState([]);
  const [tags, setTags] = useState([]);
  const [comments, setComments] = useState([]);

  const [projectFilter, setProjectFilter] = useState('all');
  const [userSearch, setUserSearch] = useState('');

  const [taskModal, setTaskModal] = useState({ open: false, mode: 'create', task: null });
  const [projectModal, setProjectModal] = useState({ open: false, project: null });
  const [taskDetailsModal, setTaskDetailsModal] = useState({ open: false, taskId: null, source: 'board' });
  const [commentModal, setCommentModal] = useState({ open: false, taskId: null });
  const [tagModal, setTagModal] = useState({ open: false, taskId: null });
  const [selectedUser, setSelectedUser] = useState(null);

  useEffect(() => {
    if (!user) {
      clearSession();
      navigate('/signin', { replace: true });
      return;
    }

    loadData();
  }, []);

  async function loadData() {
    setLoading(true);
    setError('');

    try {
      const [usersData, projectsData, tasksData, tagsData, commentsData] = await Promise.all([
        apiFetch('/api/users'),
        apiFetch('/api/projects'),
        apiFetch('/api/tasks'),
        apiFetch('/api/tags'),
        apiFetch('/api/comments')
      ]);

      setUsers(usersData);
      setProjects(projectsData);
      setTasks(tasksData);
      setTags(tagsData);
      setComments(commentsData);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  function logout() {
    clearSession();
    navigate('/signin', { replace: true });
  }

  const myProjects = useMemo(
    () => projects.filter((project) => project.ownerId === user?.id),
    [projects, user]
  );

  const myProjectIds = useMemo(
    () => new Set(myProjects.map((project) => project.id)),
    [myProjects]
  );

  const myTasks = useMemo(() => {
    const visible = tasks.filter((task) => task.assigneeId === user?.id || myProjectIds.has(task.projectId));

    if (projectFilter === 'all') {
      return visible;
    }

    return visible.filter((task) => task.projectId === Number(projectFilter));
  }, [tasks, user, myProjectIds, projectFilter]);

  const tasksByStatus = useMemo(
    () => STATUSES.reduce((acc, status) => {
      acc[status] = myTasks.filter((task) => task.status === status);
      return acc;
    }, {}),
    [myTasks]
  );

  const myTasksByProjectAndStatus = useMemo(
    () => myProjects.reduce((acc, project) => {
      const projectTasks = myTasks.filter((task) => task.projectId === project.id);
      acc[project.id] = STATUSES.reduce((statusAcc, status) => {
        statusAcc[status] = projectTasks.filter((task) => task.status === status);
        return statusAcc;
      }, {});
      return acc;
    }, {}),
    [myProjects, myTasks]
  );

  const activeTask = useMemo(
    () => tasks.find((task) => task.id === taskDetailsModal.taskId) || null,
    [tasks, taskDetailsModal.taskId]
  );

  const searchedUsers = useMemo(() => {
    const q = userSearch.trim().toLowerCase();
    if (!q) {
      return users;
    }

    return users.filter((candidate) => candidate.username.toLowerCase().includes(q));
  }, [users, userSearch]);

  function projectName(projectId) {
    return projects.find((project) => project.id === projectId)?.name || `Project #${projectId}`;
  }

  function username(userId) {
    return users.find((candidate) => candidate.id === userId)?.username || `User #${userId}`;
  }

  function userTasks(candidate) {
    return tasks.filter(
      (task) => task.assigneeId === candidate.id
        || projects.find((project) => project.id === task.projectId)?.ownerId === candidate.id
    );
  }

  async function submitProject(payload) {
    let savedProject;
    if (projectModal.project) {
      savedProject = await apiFetch(`/api/projects/${projectModal.project.id}`, {
        method: 'PUT',
        body: JSON.stringify(payload)
      });
      setProjects((prev) => prev.map((project) => (
        project.id === savedProject.id ? savedProject : project
      )));
    } else {
      savedProject = await apiFetch('/api/projects', {
        method: 'POST',
        body: JSON.stringify(payload)
      });
      setProjects((prev) => [...prev, savedProject]);
    }

    setProjectModal({ open: false, project: null });
  }

  async function removeProject(projectId) {
    const hasTasks = tasks.some((task) => task.projectId === projectId);
    if (hasTasks) {
      alert('Move or delete tasks in this project before removing it.');
      return;
    }

    await apiFetch(`/api/projects/${projectId}`, { method: 'DELETE' });
    if (String(projectId) === projectFilter) {
      setProjectFilter('all');
    }
    setProjects((prev) => prev.filter((project) => project.id !== projectId));
    setTasks((prev) => prev.filter((task) => task.projectId !== projectId));
  }

  async function submitTask(payload) {
    const requestPayload = {
      ...payload,
      assigneeId: user.id
    };

    let savedTask;
    if (taskModal.mode === 'edit' && taskModal.task) {
      savedTask = await apiFetch(`/api/tasks/${taskModal.task.id}`, {
        method: 'PUT',
        body: JSON.stringify(requestPayload)
      });
      setTasks((prev) => prev.map((task) => (task.id === savedTask.id ? savedTask : task)));
    } else {
      savedTask = await apiFetch('/api/tasks', {
        method: 'POST',
        body: JSON.stringify(requestPayload)
      });
      setTasks((prev) => [...prev, savedTask]);
    }

    setTaskModal({ open: false, mode: 'create', task: null });
  }

  async function removeTask(taskId) {
    await apiFetch(`/api/tasks/${taskId}`, { method: 'DELETE' });
    if (taskDetailsModal.taskId === taskId) {
      setTaskDetailsModal({ open: false, taskId: null, source: 'board' });
    }
    setTasks((prev) => prev.filter((task) => task.id !== taskId));
    setComments((prev) => prev.filter((comment) => comment.taskId !== taskId));
  }

  async function addComment(taskId, text) {
    const created = await apiFetch('/api/comments', {
      method: 'POST',
      body: JSON.stringify({ text, taskId, authorId: user.id })
    });

    setComments((prev) => [...prev, created]);
  }

  async function submitComment(text) {
    if (!commentModal.taskId) {
      return;
    }
    await addComment(commentModal.taskId, text.trim());
    setCommentModal({ open: false, taskId: null });
  }

  async function createTag(name) {
    const normalizedName = name.trim();
    if (!normalizedName) {
      throw new Error('Tag name is required');
    }

    const existingTag = tags.find(
      (tag) => tag.name.trim().toLowerCase() === normalizedName.toLowerCase()
    );
    if (existingTag) {
      return existingTag;
    }

    const createdTag = await apiFetch('/api/tags', {
      method: 'POST',
      body: JSON.stringify({ name: normalizedName })
    });

    setTags((prev) => [...prev, createdTag]);
    return createdTag;
  }

  async function submitTaskTags(tagIds) {
    if (!tagModal.taskId) {
      return;
    }

    const targetTask = tasks.find((task) => task.id === tagModal.taskId);
    if (!targetTask) {
      setTagModal({ open: false, taskId: null });
      return;
    }

    const updatedTask = await apiFetch(`/api/tasks/${targetTask.id}`, {
      method: 'PUT',
      body: JSON.stringify(taskToRequest(targetTask, { tagIds }))
    });

    setTasks((prev) => prev.map((task) => (task.id === updatedTask.id ? updatedTask : task)));
    setTagModal({ open: false, taskId: null });
  }

  function openTaskDetails(taskId, source = 'board') {
    setTaskDetailsModal({ open: true, taskId, source });
  }

  function closeTaskDetails() {
    setTaskDetailsModal({ open: false, taskId: null, source: 'board' });
  }

  if (loading) {
    return <div className="board-loading">Loading dashboard...</div>;
  }

  if (error) {
    return (
      <div className="board-loading">
        <p>Failed to load data: {error}</p>
        <button className="btn btn-primary" onClick={loadData}>Retry</button>
      </div>
    );
  }

  return (
    <div className="dashboard-layout">
      <header className="app-topbar">
        <div className="app-topbar-brand">
          <img src={googleTasksLogo} alt="Task Manager logo" />
          <div>
            <strong>Task Manager</strong>
            <small>Personal workspace</small>
          </div>
        </div>
        <div className="app-topbar-actions">
          <button className="topbar-profile-btn" type="button" onClick={() => setSelectedUser(user)}>
            <AvatarBadge name={user.username} />
            <span>{user.username}</span>
          </button>
          <button className="btn btn-topbar-ghost" onClick={logout}>Logout</button>
        </div>
      </header>

      <div className="board-shell">
        <aside className="left-panel card">
          <div>
            <h3>Projects</h3>
            <button className="btn btn-soft full" onClick={() => setProjectModal({ open: true, project: null })}>
              + New project
            </button>
          </div>

          <div className="project-filter">
            <button
              className={projectFilter === 'all' ? 'pill active' : 'pill'}
              onClick={() => setProjectFilter('all')}
            >
              All my tasks
            </button>
            {myProjects.map((project) => (
              <div className="project-row" key={project.id}>
                <div className="project-main-row">
                  <button
                    className={String(project.id) === projectFilter ? 'pill active' : 'pill'}
                    onClick={() => setProjectFilter(String(project.id))}
                  >
                    <span>{project.name}</span>
                  </button>
                  <div className="row-actions">
                    <button className="icon-btn" onClick={() => setProjectModal({ open: true, project })}>
                      <PencilIcon />
                    </button>
                    <button className="icon-btn danger" onClick={() => removeProject(project.id)}>
                      <TrashIcon />
                    </button>
                  </div>
                </div>
                <div className="project-tasks-preview">
                  {STATUSES.map((status) => (
                    <div className="project-status-block" key={`${project.id}-${status}`}>
                      <div className="project-status-head">
                        <span>{STATUS_LABEL[status]}</span>
                        <b>{myTasksByProjectAndStatus[project.id]?.[status]?.length || 0}</b>
                      </div>
                      <div className="project-status-list">
                        {(myTasksByProjectAndStatus[project.id]?.[status] || []).slice(0, 3).map((task) => (
                          <button
                            key={task.id}
                            type="button"
                            className="project-task-link"
                            onClick={(event) => {
                              event.stopPropagation();
                              openTaskDetails(task.id, 'preview');
                            }}
                          >
                            {task.title}
                          </button>
                        ))}
                        {(myTasksByProjectAndStatus[project.id]?.[status] || []).length === 0 && (
                          <span className="muted">No tasks</span>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            ))}
          </div>
        </aside>

        <main className="center-panel">
          <header className="board-top card">
            <div className="board-top-left">
              <h2>Task board</h2>
              <p className="muted">{user.username}, here are only your tasks.</p>
            </div>
            <button
              className="btn btn-primary"
              disabled={!myProjects.length}
              onClick={() => {
                const defaultProjectId = projectFilter === 'all' ? myProjects[0]?.id : Number(projectFilter);
                setTaskModal({
                  open: true,
                  mode: 'create',
                  task: taskToRequest(null, { projectId: defaultProjectId || '' })
                });
              }}
            >
              + Add task
            </button>
          </header>

          <section className="status-grid">
            {STATUSES.map((status) => (
              <div className={`status-column card status-column-${statusToClass(status)}`} key={status}>
                <header>
                  <div className="status-title-badge">
                    <h3>{STATUS_LABEL[status]}</h3>
                  </div>
                  <span className="status-count-badge">{tasksByStatus[status].length}</span>
                </header>
                <div className="card-list">
                  {tasksByStatus[status].map((task) => (
                    <article
                      className="task-card clickable"
                      key={task.id}
                      onClick={() => openTaskDetails(task.id)}
                    >
                      <div className="task-actions">
                        <button
                          className="icon-btn"
                          onClick={(event) => {
                            event.stopPropagation();
                            setTaskModal({ open: true, mode: 'edit', task });
                          }}
                        >
                          <PencilIcon />
                        </button>
                        <button
                          className="icon-btn danger"
                          onClick={(event) => {
                            event.stopPropagation();
                            removeTask(task.id);
                          }}
                        >
                          <TrashIcon />
                        </button>
                      </div>
                      <h4>{task.title}</h4>
                      <p className="muted clamp">{task.description || 'No description'}</p>
                      <div className="meta-grid">
                        <span><b>Owner:</b> {username(projects.find((p) => p.id === task.projectId)?.ownerId)}</span>
                        <span><b>Status:</b> {STATUS_LABEL[task.status]}</span>
                        <span><b>Due:</b> {task.dueDate || 'N/A'}</span>
                        <span><b>Project:</b> {projectName(task.projectId)}</span>
                      </div>
                      <div className="tag-row">
                        {(task.tagIds || []).map((tagId) => (
                          <span className="chip" key={tagId}>#{tags.find((tag) => tag.id === tagId)?.name || tagId}</span>
                        ))}
                      </div>
                      <div className="task-card-actions">
                        <button
                          className="btn btn-ghost full"
                          onClick={(event) => {
                            event.stopPropagation();
                            setTagModal({ open: true, taskId: task.id });
                          }}
                        >
                          Manage Tag
                        </button>
                        <button
                          className="btn btn-ghost full"
                          onClick={(event) => {
                            event.stopPropagation();
                            setCommentModal({ open: true, taskId: task.id });
                          }}
                        >
                          Add Comment
                        </button>
                      </div>
                    </article>
                  ))}
                </div>
              </div>
            ))}
          </section>
        </main>

        <aside className="right-panel card">
          <h3>User search</h3>
          <input
            placeholder="Search users by username"
            value={userSearch}
            onChange={(e) => setUserSearch(e.target.value)}
          />

          <div className="users-list">
            {searchedUsers.map((candidate) => {
              const candidateTaskList = userTasks(candidate);

              return (
                <article className="user-card clickable" key={candidate.id} onClick={() => setSelectedUser(candidate)}>
                  <header>
                    <div className="user-head">
                      <AvatarBadge name={candidate.username} />
                      <strong>{candidate.username}</strong>
                    </div>
                    <span>{candidateTaskList.length} tasks</span>
                  </header>
                  <p>{candidate.email}</p>
                  <div className="tiny-list">
                    <strong className="tiny-list-title">Task:</strong>
                    {candidateTaskList.slice(0, 3).map((task, index) => (
                      <span key={task.id}>{index + 1}. {task.title}</span>
                    ))}
                    {!candidateTaskList.length && <span>1. No planned tasks</span>}
                  </div>
                </article>
              );
            })}
          </div>
        </aside>
      </div>

      {taskModal.open && (
        <TaskModal
          mode={taskModal.mode}
          task={taskModal.task}
          projects={myProjects}
          tags={tags}
          currentUserName={user.username}
          onClose={() => setTaskModal({ open: false, mode: 'create', task: null })}
          onSubmit={submitTask}
        />
      )}

      {projectModal.open && (
        <ProjectModal
          project={projectModal.project}
          ownerId={user.id}
          onClose={() => setProjectModal({ open: false, project: null })}
          onSubmit={submitProject}
        />
      )}

      {taskDetailsModal.open && activeTask && (
        <TaskDetailsModal
          task={activeTask}
          users={users}
          tags={tags}
          projectName={projectName(activeTask.projectId)}
          comments={comments.filter((comment) => comment.taskId === activeTask.id)}
          isCommentModalOpen={commentModal.open && commentModal.taskId === activeTask.id}
          isTagModalOpen={tagModal.open && tagModal.taskId === activeTask.id}
          showManageActions={taskDetailsModal.source !== 'preview'}
          onOpenAddComment={(taskId) => setCommentModal({ open: true, taskId })}
          onOpenTagManager={(taskId) => setTagModal({ open: true, taskId })}
          onClose={closeTaskDetails}
        />
      )}

      {commentModal.open && (
        <AddCommentModal
          task={tasks.find((task) => task.id === commentModal.taskId) || null}
          onClose={() => setCommentModal({ open: false, taskId: null })}
          onSubmit={submitComment}
        />
      )}

      {tagModal.open && (
        <TaskTagsModal
          task={tasks.find((task) => task.id === tagModal.taskId) || null}
          tags={tags}
          onClose={() => setTagModal({ open: false, taskId: null })}
          onCreateTag={createTag}
          onSubmit={submitTaskTags}
        />
      )}

      {selectedUser && (
        <UserDetailsModal
          user={selectedUser}
          tasks={userTasks(selectedUser)}
          onClose={() => setSelectedUser(null)}
        />
      )}
    </div>
  );
}

function TaskModal({ mode, task, projects, tags, currentUserName, onClose, onSubmit }) {
  const [form, setForm] = useState(
    taskToRequest(task, {
      projectId: task?.projectId || projects[0]?.id || '',
      status: task?.status || 'TODO'
    })
  );

  const editMode = mode === 'edit';

  function update(name, value) {
    setForm((prev) => ({ ...prev, [name]: value }));
  }

  function toggleTag(tagId) {
    setForm((prev) => {
      const exists = prev.tagIds.includes(tagId);
      return {
        ...prev,
        tagIds: exists ? prev.tagIds.filter((id) => id !== tagId) : [...prev.tagIds, tagId]
      };
    });
  }

  function submit(event) {
    event.preventDefault();
    onSubmit({
      ...form,
      projectId: Number(form.projectId)
    });
  }

  return (
    <div className="overlay" onClick={onClose}>
      <form className="modal card" onClick={(e) => e.stopPropagation()} onSubmit={submit}>
        <h3>{editMode ? 'Edit task' : 'Create task'}</h3>

        <label className="field">
          <span>Assignee</span>
          <input value={currentUserName} disabled />
        </label>

        <label className="field">
          <span>Title</span>
          <input required maxLength={200} value={form.title} onChange={(e) => update('title', e.target.value)} />
        </label>

        <label className="field">
          <span>Description</span>
          <textarea maxLength={2000} value={form.description} onChange={(e) => update('description', e.target.value)} />
        </label>

        <div className="grid-two">
          <label className="field">
            <span>Status</span>
            <select value={form.status} onChange={(e) => update('status', e.target.value)}>
              {STATUSES.map((status) => (
                <option key={status} value={status}>{STATUS_LABEL[status]}</option>
              ))}
            </select>
          </label>

          <label className="field">
            <span>Due date</span>
            <input type="date" value={form.dueDate || ''} onChange={(e) => update('dueDate', e.target.value)} />
          </label>
        </div>

        <label className="field">
          <span>Project</span>
          <select required value={form.projectId} onChange={(e) => update('projectId', e.target.value)}>
            <option value="" disabled>Select project</option>
            {projects.map((project) => (
              <option key={project.id} value={project.id}>{project.name}</option>
            ))}
          </select>
        </label>

        <div className="tags-picker">
          {tags.map((tag) => (
            <button
              key={tag.id}
              type="button"
              className={form.tagIds.includes(tag.id) ? 'chip active' : 'chip'}
              onClick={() => toggleTag(tag.id)}
            >
              #{tag.name}
            </button>
          ))}
        </div>

        <div className="dialog-actions">
          <button type="button" className="btn btn-ghost" onClick={onClose}>Cancel</button>
          <button type="submit" className="btn btn-primary">Save</button>
        </div>
      </form>
    </div>
  );
}

function ProjectModal({ project, ownerId, onClose, onSubmit }) {
  const [name, setName] = useState(project?.name || '');
  const [description, setDescription] = useState(project?.description || '');
  const [submitError, setSubmitError] = useState('');

  async function submit(event) {
    event.preventDefault();
    setSubmitError('');
    try {
      await onSubmit({ name, description, ownerId });
    } catch (err) {
      setSubmitError(err.message || 'Failed to save project');
    }
  }

  return (
    <div className="overlay" onClick={onClose}>
      <form className="modal card" onClick={(e) => e.stopPropagation()} onSubmit={submit}>
        <h3>{project ? 'Edit project' : 'Create project'}</h3>
        {submitError && <div className="error-box">{submitError}</div>}
        <label className="field">
          <span>Name</span>
          <input required maxLength={150} value={name} onChange={(e) => setName(e.target.value)} />
        </label>
        <label className="field">
          <span>Description</span>
          <textarea maxLength={1000} value={description} onChange={(e) => setDescription(e.target.value)} />
        </label>
        <div className="dialog-actions">
          <button type="button" className="btn btn-ghost" onClick={onClose}>Cancel</button>
          <button type="submit" className="btn btn-primary">Save</button>
        </div>
      </form>
    </div>
  );
}

function TaskDetailsModal({
  task,
  tags,
  projectName,
  comments,
  users,
  isCommentModalOpen,
  isTagModalOpen,
  showManageActions,
  onOpenAddComment,
  onOpenTagManager,
  onClose
}) {
  const taskTagIds = task.tagIds || [];

  return (
    <div className="overlay" onClick={onClose}>
      <aside className="modal card details-modal task-details-modal" onClick={(e) => e.stopPropagation()}>
        <header className="task-details-head">
          <div>
            <p className="muted">Task details</p>
            <h3>{task.title}</h3>
          </div>
          <button className="icon-btn" onClick={onClose}>x</button>
        </header>

        <section className="task-details-meta">
          <span><b>Status:</b> {STATUS_LABEL[task.status]}</span>
          <span><b>Due:</b> {task.dueDate || 'N/A'}</span>
          <span><b>Project:</b> {projectName}</span>
          <span><b>Assignee:</b> {users.find((u) => u.id === task.assigneeId)?.username || task.assigneeId}</span>
        </section>

        <section className="task-description-block">
          <h4>Description</h4>
          <p>{task.description || 'No description'}</p>
        </section>

        <section>
          <div className="section-head">
            <h4>Tags</h4>
            {showManageActions && (
              <button className="btn btn-soft" type="button" onClick={() => onOpenTagManager(task.id)}>+ Add tag</button>
            )}
          </div>
          <div className="tags-picker">
            {taskTagIds.map((tagId) => (
              <span className="chip active" key={tagId}>
                #{tags.find((tag) => tag.id === tagId)?.name || tagId}
              </span>
            ))}
          </div>
          {!taskTagIds.length && !isTagModalOpen && <p className="muted">No tags yet.</p>}
        </section>

        <section>
          <div className="section-head">
            <h4>Comments</h4>
            {showManageActions && (
              <button className="btn btn-soft" type="button" onClick={() => onOpenAddComment(task.id)}>+ Add comment</button>
            )}
          </div>
          <div className="comment-list scrollable-comments">
            {comments.map((comment) => (
              <article className="comment" key={comment.id}>
                <header>
                  <div className="user-head">
                    <AvatarBadge name={users.find((u) => u.id === comment.authorId)?.username || String(comment.authorId)} />
                    <strong>{users.find((u) => u.id === comment.authorId)?.username || comment.authorId}</strong>
                  </div>
                  <span>{new Date(comment.createdAt).toLocaleString()}</span>
                </header>
                <p>{comment.text}</p>
              </article>
            ))}
          </div>
          {!comments.length && !isCommentModalOpen && <p className="muted">No comments yet.</p>}
        </section>
      </aside>
    </div>
  );
}

function AddCommentModal({ task, onClose, onSubmit }) {
  const [text, setText] = useState('');
  const [submitError, setSubmitError] = useState('');
  const [busy, setBusy] = useState(false);

  async function submit(event) {
    event.preventDefault();
    const normalizedText = text.trim();
    if (!normalizedText) {
      return;
    }

    setSubmitError('');
    setBusy(true);
    try {
      await onSubmit(normalizedText);
    } catch (err) {
      setSubmitError(err.message || 'Failed to add comment');
      setBusy(false);
    }
  }

  return (
    <div className="overlay" onClick={onClose}>
      <form className="modal card form-modal" onClick={(e) => e.stopPropagation()} onSubmit={submit}>
        <h3>Add comment</h3>
        {task && <p className="muted">Task: {task.title}</p>}
        {submitError && <div className="error-box">{submitError}</div>}
        <label className="field">
          <span>Comment text</span>
          <textarea
            required
            maxLength={2000}
            autoFocus
            value={text}
            onChange={(event) => setText(event.target.value)}
            placeholder="Write your comment..."
          />
        </label>
        <div className="dialog-actions">
          <button type="button" className="btn btn-ghost" onClick={onClose}>Cancel</button>
          <button type="submit" className="btn btn-primary" disabled={busy || !text.trim()}>
            {busy ? 'Saving...' : 'Add comment'}
          </button>
        </div>
      </form>
    </div>
  );
}

function TaskTagsModal({ task, tags, onClose, onCreateTag, onSubmit }) {
  const [selectedTagIds, setSelectedTagIds] = useState(task?.tagIds || []);
  const [newTagName, setNewTagName] = useState('');
  const [submitError, setSubmitError] = useState('');
  const [busy, setBusy] = useState(false);
  const [creatingTag, setCreatingTag] = useState(false);

  function toggleTag(tagId) {
    setSelectedTagIds((prev) => (
      prev.includes(tagId) ? prev.filter((id) => id !== tagId) : [...prev, tagId]
    ));
  }

  async function createNewTag(event) {
    event.preventDefault();
    const normalizedName = newTagName.trim();
    if (!normalizedName) {
      return;
    }

    setSubmitError('');
    setCreatingTag(true);
    try {
      const createdTag = await onCreateTag(normalizedName);
      setSelectedTagIds((prev) => (
        prev.includes(createdTag.id) ? prev : [...prev, createdTag.id]
      ));
      setNewTagName('');
    } catch (err) {
      setSubmitError(err.message || 'Failed to create tag');
    } finally {
      setCreatingTag(false);
    }
  }

  async function submit(event) {
    event.preventDefault();
    if (!task) {
      return;
    }

    setSubmitError('');
    setBusy(true);
    try {
      await onSubmit(selectedTagIds);
    } catch (err) {
      setSubmitError(err.message || 'Failed to update task tags');
      setBusy(false);
    }
  }

  return (
    <div className="overlay" onClick={onClose}>
      <form className="modal card form-modal" onClick={(e) => e.stopPropagation()} onSubmit={submit}>
        <h3>Manage tags</h3>
        {task && <p className="muted">Task: {task.title}</p>}
        {submitError && <div className="error-box">{submitError}</div>}
        <div className="field">
          <span>Create a new tag</span>
          <div className="tag-create-row">
            <input
              maxLength={80}
              placeholder="For example: design"
              value={newTagName}
              onChange={(event) => setNewTagName(event.target.value)}
            />
            <button
              className="btn btn-soft tag-create-btn"
              type="button"
              onClick={createNewTag}
              disabled={creatingTag || !newTagName.trim()}
            >
              {creatingTag ? 'Creating...' : 'Create tag'}
            </button>
          </div>
        </div>
        <div className="tags-picker">
          {tags.map((tag) => (
            <button
              key={tag.id}
              type="button"
              className={selectedTagIds.includes(tag.id) ? 'chip active' : 'chip'}
              onClick={() => toggleTag(tag.id)}
            >
              #{tag.name}
            </button>
          ))}
        </div>
        {!tags.length && <p className="muted">No tags available.</p>}
        <div className="dialog-actions">
          <button type="button" className="btn btn-ghost" onClick={onClose}>Cancel</button>
          <button type="submit" className="btn btn-primary" disabled={busy}>
            {busy ? 'Saving...' : 'Save tags'}
          </button>
        </div>
      </form>
    </div>
  );
}

function UserDetailsModal({ user, tasks, onClose }) {
  const tasksByStatus = {
    TODO: tasks.filter((task) => task.status === 'TODO'),
    IN_PROGRESS: tasks.filter((task) => task.status === 'IN_PROGRESS'),
    DONE: tasks.filter((task) => task.status === 'DONE')
  };

  const counts = {
    TODO: tasksByStatus.TODO.length,
    IN_PROGRESS: tasksByStatus.IN_PROGRESS.length,
    DONE: tasksByStatus.DONE.length
  };

  return (
    <div className="overlay" onClick={onClose}>
      <article className="modal card user-modal" onClick={(e) => e.stopPropagation()}>
        <header className="user-modal-head">
          <div className="user-head big">
            <AvatarBadge name={user.username} large />
            <div>
              <h3>{user.username}</h3>
              <p className="muted">{user.email}</p>
            </div>
          </div>
          <button className="icon-btn" onClick={onClose}>x</button>
        </header>

        <div className="user-stats">
          <span>To Do: {counts.TODO}</span>
          <span>In Progress: {counts.IN_PROGRESS}</span>
          <span>Done: {counts.DONE}</span>
        </div>

        <div className="user-task-columns">
          {STATUSES.map((status) => (
            <div className="user-task-column" key={status}>
              <h4>{STATUS_LABEL[status]}</h4>
              <div className="user-task-list">
                {tasksByStatus[status].map((task) => (
                  <article className="user-task-card" key={task.id}>{task.title}</article>
                ))}
                {!tasksByStatus[status].length && <span className="muted">No tasks</span>}
              </div>
            </div>
          ))}
        </div>
      </article>
    </div>
  );
}

function taskToRequest(task, overrides = {}) {
  return {
    title: task?.title || '',
    description: task?.description || '',
    status: task?.status || 'TODO',
    dueDate: task?.dueDate || null,
    projectId: task?.projectId || null,
    assigneeId: task?.assigneeId || null,
    tagIds: task?.tagIds || [],
    ...overrides
  };
}

function AvatarBadge({ name, large = false }) {
  const initial = (name || '?').trim().charAt(0).toUpperCase() || '?';
  return <div className={large ? 'avatar-badge large' : 'avatar-badge'}>{initial}</div>;
}

function statusToClass(status) {
  return status.toLowerCase().replace('_', '-');
}

function PencilIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true">
      <path d="M3 17.2V21h3.8l11-11-3.8-3.8-11 11Zm17.7-10.4c.4-.4.4-1.1 0-1.5l-2-2a1.1 1.1 0 0 0-1.5 0l-1.6 1.6 3.8 3.8 1.3-2Z" />
    </svg>
  );
}

function TrashIcon() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true">
      <path d="M6 7h12l-1 14a2 2 0 0 1-2 2H9a2 2 0 0 1-2-2L6 7Zm3-4h6l1 2h4v2H4V5h4l1-2Z" />
    </svg>
  );
}

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<LandingPage />} />
      <Route path="/signin" element={<AuthPage mode="signin" />} />
      <Route path="/signup" element={<AuthPage mode="signup" />} />
      <Route
        path="/app"
        element={(
          <ProtectedRoute>
            <DashboardPage />
          </ProtectedRoute>
        )}
      />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
