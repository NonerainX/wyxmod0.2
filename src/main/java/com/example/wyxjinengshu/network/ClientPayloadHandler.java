package com.example.wyxjinengshu.network;

import com.example.wyxjinengshu.skills.Skill;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 客户端技能数据缓存 + 同步包处理。
 */
public final class ClientPayloadHandler {

    public static final ClientSkillCache CACHE = new ClientSkillCache();

    private ClientPayloadHandler() {
    }

    public static void handleSync(final SyncSkillDataPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> CACHE.update(payload.points(), payload.levels()));
    }

    public static final class ClientSkillCache {
        private int points;
        private int[] levels = new int[Skill.ALL.size()];

        public synchronized void update(int points, int[] levels) {
            this.points = Math.max(0, points);
            this.levels = new int[Skill.ALL.size()];
            for (int i = 0; i < this.levels.length; i++) {
                if (i < levels.length) {
                    this.levels[i] = Math.max(0, Math.min(Skill.ALL.get(i).getMaxLevel(), levels[i]));
                }
            }
        }

        public synchronized int points() {
            return points;
        }

        public synchronized int level(Skill skill) {
            int i = Skill.indexOf(skill);
            return i >= 0 ? levels[i] : 0;
        }
    }
}