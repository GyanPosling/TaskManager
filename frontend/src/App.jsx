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
import defaultAvatar from './assets/default-avatar.jpg';
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
  const [detailsTask, setDetailsTask] = useState(null);
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
    if (projectModal.project) {
      await apiFetch(`/api/projects/${projectModal.project.id}`, {
        method: 'PUT',
        body: JSON.stringify(payload)
      });
    } else {
      await apiFetch('/api/projects', {
        method: 'POST',
        body: JSON.stringify(payload)
      });
    }

    setProjectModal({ open: false, project: null });
    await loadData();
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
    await loadData();
  }

  async function submitTask(payload) {
    const requestPayload = {
      ...payload,
      assigneeId: user.id
    };

    if (taskModal.mode === 'edit' && taskModal.task) {
      await apiFetch(`/api/tasks/${taskModal.task.id}`, {
        method: 'PUT',
        body: JSON.stringify(requestPayload)
      });
    } else {
      await apiFetch('/api/tasks', {
        method: 'POST',
        body: JSON.stringify(requestPayload)
      });
    }

    setTaskModal({ open: false, mode: 'create', task: null });
    await loadData();
  }

  async function removeTask(taskId) {
    await apiFetch(`/api/tasks/${taskId}`, { method: 'DELETE' });
    if (detailsTask?.id === taskId) {
      setDetailsTask(null);
    }
    await loadData();
  }

  async function addComment(taskId, text) {
    await apiFetch('/api/comments', {
      method: 'POST',
      body: JSON.stringify({ text, taskId, authorId: user.id })
    });

    await loadData();
  }

  async function createTag(name) {
    const created = await apiFetch('/api/tags', {
      method: 'POST',
      body: JSON.stringify({ name })
    });

    setTags((prev) => [...prev, created]);
    return created.id;
  }

  async function appendTag(task, tagId) {
    const nextTagIds = Array.from(new Set([...(task.tagIds || []), tagId]));
    await apiFetch(`/api/tasks/${task.id}`, {
      method: 'PUT',
      body: JSON.stringify(taskToRequest(task, { tagIds: nextTagIds, assigneeId: user.id }))
    });

    await loadData();
  }

  async function removeTag(task, tagId) {
    const nextTagIds = (task.tagIds || []).filter((id) => id !== tagId);
    await apiFetch(`/api/tasks/${task.id}`, {
      method: 'PUT',
      body: JSON.stringify(taskToRequest(task, { tagIds: nextTagIds, assigneeId: user.id }))
    });

    await loadData();
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
              <button
                className={String(project.id) === projectFilter ? 'pill active' : 'pill'}
                onClick={() => setProjectFilter(String(project.id))}
              >
                {project.name}
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
          ))}
        </div>

        <button className="btn btn-ghost" onClick={logout}>Logout</button>
      </aside>

      <main className="center-panel">
        <header className="board-top card">
          <div className="board-top-left">
            <div className="board-brand">
              <img src={googleTasksLogo} alt="Task Manager logo" />
              <div>
                <strong>Task Manager</strong>
                <small>Personal workspace</small>
              </div>
            </div>
            <h2>Task board</h2>
            <p className="muted">{user.username}, here are only your tasks.</p>
          </div>
          <button
            className="btn btn-primary"
            disabled={!myProjects.length}
            onClick={() => setTaskModal({ open: true, mode: 'create', task: null })}
          >
            + Add task
          </button>
        </header>

        <section className="status-grid">
          {STATUSES.map((status) => (
            <div className="status-column card" key={status}>
              <header>
                <h3>{STATUS_LABEL[status]}</h3>
                <span>{tasksByStatus[status].length}</span>
              </header>
              <div className="card-list">
                {tasksByStatus[status].map((task) => (
                  <article className="task-card" key={task.id}>
                    <div className="task-actions">
                      <button className="icon-btn" onClick={() => setTaskModal({ open: true, mode: 'edit', task })}>
                        <PencilIcon />
                      </button>
                      <button className="icon-btn danger" onClick={() => removeTask(task.id)}>
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
                      {(task.tagIds || []).slice(0, 4).map((tagId) => (
                        <span className="chip" key={tagId}>#{tags.find((tag) => tag.id === tagId)?.name || tagId}</span>
                      ))}
                    </div>
                    <button className="btn btn-ghost full" onClick={() => setDetailsTask(task)}>
                      Comments & tags
                    </button>
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
                    <img src={defaultAvatar} alt="User avatar" />
                    <strong>{candidate.username}</strong>
                  </div>
                  <span>{candidateTaskList.length} tasks</span>
                </header>
                <p>{candidate.email}</p>
                <div className="tiny-list">
                  {candidateTaskList.slice(0, 3).map((task) => (
                    <span key={task.id}>{task.title}</span>
                  ))}
                  {!candidateTaskList.length && <span>No planned tasks</span>}
                </div>
              </article>
            );
          })}
        </div>
      </aside>

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

      {detailsTask && (
        <TaskDetailsDrawer
          task={detailsTask}
          tags={tags}
          comments={comments.filter((comment) => comment.taskId === detailsTask.id)}
          users={users}
          onClose={() => setDetailsTask(null)}
          onAddComment={addComment}
          onCreateTag={createTag}
          onAttachTag={appendTag}
          onRemoveTag={removeTag}
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

function TaskDetailsDrawer({
  task,
  tags,
  comments,
  users,
  onClose,
  onAddComment,
  onCreateTag,
  onAttachTag,
  onRemoveTag
}) {
  const [commentText, setCommentText] = useState('');
  const [tagName, setTagName] = useState('');
  const [selectedTagId, setSelectedTagId] = useState('');

  async function submitComment(event) {
    event.preventDefault();
    if (!commentText.trim()) {
      return;
    }

    await onAddComment(task.id, commentText.trim());
    setCommentText('');
  }

  async function submitTag(event) {
    event.preventDefault();

    if (tagName.trim()) {
      const createdId = await onCreateTag(tagName.trim());
      await onAttachTag(task, createdId);
      setTagName('');
      return;
    }

    if (selectedTagId) {
      await onAttachTag(task, Number(selectedTagId));
      setSelectedTagId('');
    }
  }

  return (
    <div className="drawer-overlay" onClick={onClose}>
      <aside className="drawer card" onClick={(e) => e.stopPropagation()}>
        <header>
          <h3>{task.title}</h3>
          <button className="icon-btn" onClick={onClose}>x</button>
        </header>

        <section>
          <h4>Tags</h4>
          <div className="tags-picker">
            {(task.tagIds || []).map((tagId) => (
              <button className="chip active" key={tagId} onClick={() => onRemoveTag(task, tagId)}>
                #{tags.find((tag) => tag.id === tagId)?.name || tagId} x
              </button>
            ))}
          </div>
          <form className="inline-form" onSubmit={submitTag}>
            <select value={selectedTagId} onChange={(e) => setSelectedTagId(e.target.value)}>
              <option value="">Attach existing tag</option>
              {tags.map((tag) => (
                <option key={tag.id} value={tag.id}>{tag.name}</option>
              ))}
            </select>
            <input placeholder="or create tag" value={tagName} onChange={(e) => setTagName(e.target.value)} />
            <button className="btn btn-soft" type="submit">Add tag</button>
          </form>
        </section>

        <section>
          <h4>Comments</h4>
          <div className="comment-list">
            {comments.map((comment) => (
              <article className="comment" key={comment.id}>
                <header>
                  <div className="user-head">
                    <img src={defaultAvatar} alt="User avatar" />
                    <strong>{users.find((u) => u.id === comment.authorId)?.username || comment.authorId}</strong>
                  </div>
                  <span>{new Date(comment.createdAt).toLocaleString()}</span>
                </header>
                <p>{comment.text}</p>
              </article>
            ))}
            {comments.length === 0 && <p className="muted">No comments yet.</p>}
          </div>
          <form className="inline-form" onSubmit={submitComment}>
            <textarea placeholder="Add comment" value={commentText} onChange={(e) => setCommentText(e.target.value)} />
            <button className="btn btn-primary" type="submit">Post comment</button>
          </form>
        </section>
      </aside>
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
            <img src={defaultAvatar} alt="User avatar" />
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
