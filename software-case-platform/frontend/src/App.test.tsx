import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import App from './App';

describe('App Rendering Test', () => {
  it('renders App without crashing and shows login or header', () => {
    render(
      <BrowserRouter>
        <App />
      </BrowserRouter>
    );

    // Expect brand title to be rendered
    const matches = screen.getAllByText(/CASE Platform/i);
    expect(matches.length).toBeGreaterThan(0);
  });
});
