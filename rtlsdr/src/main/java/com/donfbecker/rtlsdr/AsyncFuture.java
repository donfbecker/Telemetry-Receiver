/*
 * Copyright (C) 2022 by Signalware Ltd <driver@sdrtouch.com>
 *
 * This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.donfbecker.rtlsdr;

import androidx.annotation.NonNull;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * This is a future task that will block until result has been returned
 */
public class AsyncFuture<V> implements Future<V> {
    private final Object locker = new Object();
    private V object;
    private boolean ready = false;

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        synchronized (locker) {
            return ready;
        }
    }

    @Override
    public V get() throws InterruptedException, ExecutionException {
        synchronized (locker) {
            locker.wait();
            return object;
        }
    }

    @Override
    public V get(long timeout, @NonNull TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        synchronized (locker) {
            locker.wait(unit.toMillis(timeout));
            return object;
        }
    }

    public void setDone(V object) {
        synchronized (locker) {
            if(!this.ready) {
                this.object = object;
                this.ready = true;
                locker.notify();
            }
        }
    }
}
